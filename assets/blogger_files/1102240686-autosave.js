// Copyright 2007 Google Inc.
// All Rights Reserved.

/**
 * @fileoverview Contains Autosave class that encapsulates functionality to
 * save draft posts via xmlhttp requests based on specified criteria and on
 * demand.
 *
 * An autosave will be triggered every X milliseconds or every Y touches unless
 * the last touch was within Z milliseconds.  If X or Y have been exceeded, an
 * autosave will occur when Z is exceeded.
 *
 * Dependencies include:
 *   - dom.common.js
 *   - event.keypress.js
 *   - formcheck.js
 *   - post.images.js
 *   - posteditor.js
 *   - video.js
 *   - xmlhttp.js
 *
 * @author estebangs@google.com (Jerry Esteban)
 */

/**
 * Element id of the post editor form.
 * @type String
 */
BLOG_Autosave.POST_FORM_ID = 'stuffform';

/**
 * URL of the post autosave servlet.
 * @type String
 */
BLOG_Autosave.POST_AUTOSAVE_URL = '/post-autosave.do';

/**
 * CSS class that disables and grays out a button.
 * @type String
 */
BLOG_Autosave.DISABLED_BUTTON_CLASS = 'ubtn-disabled';

/**
 * Maximum number of milliseconds before an autosave is performed (will not
 * perform save until user pauses typing).
 * @type Number
 */
BLOG_Autosave.MAX_SAVE_TIMEOUT = 30000;

/**
 * Enum for representing running states.
 * @enum {Number}
 */
BLOG_Autosave.RunningState = {
  RUNNING: 0,
  STOPPED: 1
};

/**
 * Autosave class
 * @param {Number} interval The number of milliseconds between post autosave
 *     requests.
 * @param {Number} minTouchCount The minimum number of touches to trigger an
 *     autosave.
 * @param {Number} minTouchPause The minimum number of milliseconds between
 *     touches that is an approximation of a user pausing their typing.
 * @constructor
 */
function BLOG_Autosave(interval, minTouchCount, minTouchPause) {
  this.autosaveInterval_ = interval;
  this.minTouchCount_ = minTouchCount;
  this.minTouchPause_ = minTouchPause;

  this.saveCallback = this.createCallback(this.save);
  this.touchCallback = this.createCallback(this.touch);
}

/**
 * Minimum number of keystrokes required to trigger an autosave.
 * @type Number
 * @private
 */
BLOG_Autosave.prototype.minTouchCount_ = null;

/**
 * Minimum number of milliseconds between touches that is an approximation of
 * a user pausing their typing.
 * @type Number
 * @private
 */
BLOG_Autosave.prototype.minTouchPause_ = null;

/**
 * Number of keystrokes.
 * @type Number
 * @private
 */
BLOG_Autosave.prototype.touchCount_ = 0;

/**
 * Time of last keystroke.
 * @type Date
 * @private
 */
BLOG_Autosave.prototype.lastTouchedTimestamp_ = null;

/**
 * Id of interval timer to schedule recurring autosave.
 * @type Number
 * @private
 */
BLOG_Autosave.prototype.autosaveIntervalTimer_ = null;

/**
 * Number of milliseconds between autosave attempts.
 * @type Number
 * @private
 */
BLOG_Autosave.prototype.autosaveInterval_ = null;

/**
 * Id of timeout timer to indicate when a user has stopped typing.
 * @type Number
 * @private
 */
BLOG_Autosave.prototype.touchTimeoutTimer_ = null;

/**
 * Id of timeout timer to indicate when an attempt to perform an autosave did
 * not complete in time.
 * @type Number
 * @private
 */
BLOG_Autosave.prototype.saveTimeoutTimer_ = null;

/**
 * Inidicates the autosave's running state.
 * @type BLOG_Autosave.RunningState
 * @private
 */
BLOG_Autosave.prototype.runningState_ = BLOG_Autosave.RunningState.STOPPED;

/**
 * Represents post body data that has been saved, and is used as a baseline
 * to deterine whether any changes have been made that require an autosave.
 * @type Object
 * @private
 */
BLOG_Autosave.prototype.cleanData_ = null;

/**
 * Binds this via a closure to the callback.
 * @param {Object} fn is the function to create a callback for.
 */
BLOG_Autosave.prototype.createCallback = function(fn) {
  var self = this;
  return function() {
    var args = Array.prototype.slice.call(arguments);
    fn.apply(self, args);
  }
};

/**
 * Enables save button and starts recurring autosaves based on interval (in
 * milliseconds) passed into constructor.
 */
BLOG_Autosave.prototype.start = function() {
  if (this.runningState_ == BLOG_Autosave.RunningState.RUNNING) {
    return;
  }

  this.autosaveIntervalTimer_ = window.setInterval(this.saveCallback,
                                                   this.autosaveInterval_);
  this.runningState_ = BLOG_Autosave.RunningState.RUNNING;
};

/**
 * Disables save button and stops recurring autosaves.
 */
BLOG_Autosave.prototype.stop = function() {
  if (this.runningState_ != BLOG_Autosave.RunningState.RUNNING) {
    return;
  }

  window.clearInterval(this.autosaveIntervalTimer_);
  window.clearTimeout(this.touchTimeoutTimer_);
  this.runningState_ = BLOG_Autosave.RunningState.STOPPED;
};

/**
 * Toggles the autosave button (i.e., enable/disable).
 */
BLOG_Autosave.prototype.toggleAutosaveButton = function(enabled) {
  // The actual link
  var autosaveButton = d('autosaveButton');

  // The text of the button
  var autosaveButtonTextContainer =
      autosaveButton.getElementsByTagName('div')[2];

  if (enabled &&
      HasClass(autosaveButton, BLOG_Autosave.DISABLED_BUTTON_CLASS)) {
    // Enable save button.
    removeClass(autosaveButton, BLOG_Autosave.DISABLED_BUTTON_CLASS);
    autosaveButtonTextContainer.innerHTML = post_save_now_label;
  } else if (!enabled &&
      !HasClass(autosaveButton, BLOG_Autosave.DISABLED_BUTTON_CLASS)) {
    // Disable save button.
    addClass(autosaveButton, BLOG_Autosave.DISABLED_BUTTON_CLASS);
    autosaveButtonTextContainer.innerHTML = post_saving_label;
  }
};

/**
 * Updates the autosave object by incrementing the touch counter and last touch
 * timestamp, and represents the last time a modification was made to the post
 * form.
 */
BLOG_Autosave.prototype.touch = function(e) {
  var obj = this;

  var evt = e ? e : window.event;

  // Ignore control key commands so that keyboard shortcuts behave.
  if (isCtrlKeyPressed(e)) {
    return;
  }

  // Reenable save now button.
  obj.toggleAutosaveButton(true);

  // Starts up recurring autosave if not already running.
  obj.start();

  obj.touchCount_ += 1;
  obj.lastTouchedTimestamp_ = new Date();

  if (obj.touchCount_ > obj.minTouchCount_) {
    window.clearTimeout(obj.touchTimeoutTimer_);
    obj.touchTimeoutTimer_ = window.setTimeout(obj.saveCallback,
                                               obj.minTouchPause_);
  }
};

/**
 * Returns whether or not post form may still be being touched (i.e. modified).
 * @return {Boolean} Whether or not post is still being touched.
 */
BLOG_Autosave.prototype.isStillTouching = function() {
  var now = new Date();
  var delta = now - this.lastTouchedTimestamp_;

  return delta < this.minTouchPause_;
};

/**
 * Performs a save of the form data.  This is called in the background on a
 * recurring basis and can be called manually.  When called manually, a save is
 * performed regardless of whether or not any data modifications were made.
 */
BLOG_Autosave.prototype.save = function(e) {
  var obj = this;

  var evt = e ? e : window.event;
  var isManualSave = evt != null && (evt.type == 'click');

  var formData = obj.getFormData();
  var isChanged = obj.hasUnsavedChanges(formData);

  // Checks if there are any changes and that the post form is no longer being
  // touched.  If this was manually called by the save button, always perform
  // the save.
  if (!isManualSave && (!isChanged || obj.isStillTouching())) {
    return;
  }

  // Message to users we're getting busy.
  obj.setAutosaveMessage(false, autosave_saving_message);

  // Stop recurring autosaves.
  obj.stop();

  promoteCreateFormActionToEdit();

  var req = XH_XmlHttpCreate();
  var handler = function() {
    if (req.readyState == XML_READY_STATE_COMPLETED) {
      var failed = false;
      // Connection failures (Safari returns null, IE returns 12029, and
      // Firefox throws exception.
      try {
        if (req.status == null || req.status == 12029) {
          failed = true;
        }
      } catch (e) {
        // Firefox calls onerror() when a network error occurs.  Inside the
        // onerror handler, accessing the status attribute results in an
        // exception, which we catch here.
        failed = true;
      }

      // Clear save timeout timer (after status is verified).
      window.clearTimeout(obj.saveTimeoutTimer_);

      if (failed) {
        obj.handleNetworkError();
        return;
      }

      if (req.status == 500) {
        obj.showError([]);
        return;
      }

      var resp = eval('(' + req.responseText + ')');
      if (req.status == 200) {
        // just restore the autosave button - if someone wants to save multiple
        // times that's their business.
        obj.toggleAutosaveButton(true);
        obj.processResponse(resp, formData);
      } else {
        // Display error message and make appropriate changes to save button
        // behavior if necessary.
        obj.showError(resp);
      }
    }
  }

  // Start save timeout timer. Save now button is not changed to submit (as it
  // is for other errors), because submitting form will vomit.  So continue to
  // retry autosaving.
  obj.saveTimeoutTimer_ = window.setTimeout(
      function() {
        req.abort();
        obj.handleNetworkError();
      }, BLOG_Autosave.MAX_SAVE_TIMEOUT);


  // Flip the autosave button to disabled w/ text "saving"
  obj.toggleAutosaveButton(false);

  // Perform xmlhttp request.  If there is no post id on the form, then this is
  // a new post, so create one.  Otherwise, just edit the existing post.
  XH_XmlHttpPOST(
      req,
      BLOG_Autosave.POST_AUTOSAVE_URL,
      obj.getFormattedRequestData(formData, isManualSave),
      handler);
};

/**
 * Shows a connectivity error and restarts autosave.
 * TODO(phopkins): it would be good to backoff the autosave interval here
 */
BLOG_Autosave.prototype.handleNetworkError = function() {
  this.setAutosaveMessage(true, autosave_timeout_message);

  // autosave was stopped when save() was first called, and, since it wasn't
  // successful, we start again.
  obj.start();
};

/**
 * Resets the "clean data," which is used to determine whether or not
 * any modifications have been made to form.  This function should be called
 * after creating a new Autosave object, and before calling any other functions
 * (i.e., start, stop, etc.).
 * @param {Object} An optional map of form input name and value pairs.
 */
BLOG_Autosave.prototype.resetCleanData = function(opt_dirtyData) {
  this.cleanData_ = new Object();

  // If dirty data is not provided, initialize clean data directly from form.
  // Otherwise, populate with dirty data that has been saved.
  var dirtyData = opt_dirtyData || this.getFormData();
  for (var i in dirtyData) {
    this.cleanData_[i] = dirtyData[i];
  }
};

/**
 * Gets the post body from the post form (copied from backup.js).
 * @return {String} The post body.
 */
BLOG_Autosave.prototype.getPostBody = function() {
  if (RichEdit.mode == RichEdit.DESIGN_MODE) {
    if (Detect.IE()) {
      var body =
        RemoveLinksWithinTags(getDesignModeHtmlWithoutDOMSideEffects());
      return cleanHTML(body);
    } else {
      return getDesignModeHtml();
    }
  } else if (RichEdit.mode == RichEdit.HTML_MODE) {
    return getElement(Preview.TEXTAREA).value;
  }
};

/**
 * Returns all form input and post body data.
 * @return {Object} A map of form input and post body name and value pairs.
 */
BLOG_Autosave.prototype.getFormData = function() {
  var data = new Object();

  // Load data for all inputs..
  var inputs = d(BLOG_Autosave.POST_FORM_ID).getElementsByTagName('input');
  for (var i = 0; i < inputs.length; i++) {
    var input = inputs[i];

    // TODO(phopkins): don't keep track of submit button values

    if (input.type == 'radio' || input.type == 'checkbox') {
      // only save these buttons' values if they're turned on
      if (!input.checked) {
        continue;
      }
    }

    var name = input.name;
    var value = input.value;

    data[name] = value;
  }

  // ..and the post body.
  data['postBody'] = this.getPostBody();

  return data;
};

/**
 * Returns data that has been modified from the clean data.  This is the data
 * that will be sent to the server to be saved.
 * @return {Object} A map of form input and post body name and value pairs.
 */
BLOG_Autosave.prototype.getDirtyData = function(formData) {
  var dirtyData = new Object();

  for (var i in formData) {
    // Always include blog id, post id, and security token in post request.
    if (i == 'postID' ||
        i == 'blogID' ||
        i == 'securityToken') {
      dirtyData[i] = formData[i];
    } else if (formData[i] != this.cleanData_[i]) {
      dirtyData[i] = formData[i];
    }
  }

  return dirtyData;
};

/**
 * Indicates whether or not form has unsaved changes by comparing clean data
 * against dirty data.
 * @param {Object} dirtyData A map of form input name and value pairs, with
 *     values that may differ from clean data.
 * @return {Boolean} Whether or not form has unsaved changes.
 */
BLOG_Autosave.prototype.hasUnsavedChanges = function(dirtyData) {
  for (var i in dirtyData) {
    // Post id is ignored as this should only change when a new post is created
    // and added to the form and should not trigger an autosave.
    if (i == 'postID') {
      continue;
    } else if (dirtyData[i] != this.cleanData_[i]) {
      return true;
    }
  }

  return false;
};

/**
 * Takes form data and formats nicely to be posted via xmlhttp request.
 * @param {Object} dirtyData A map of form input name and value pairs, with
 *     values that may differ from clean data.
 * @param {Boolean} isManualSave Whether or not save was called manully by save
 *     button.
 * @return {String} Formatted name/value pairs for inputs and post body on post
 *     form.
 */
BLOG_Autosave.prototype.getFormattedRequestData = function(dirtyData,
                                                           isManualSave) {
  // If this is a manual save, then perform a deep save regardless of
  // modifications made to the form.
  var isShallowSave = !isManualSave && this.isShallowSave(dirtyData);
  var timezoneOffset = new Date().getTimezoneOffset();

  // Initialize data.
  var formattedRequestData = 'isAutosaveRequest=' + encodeURIComponent(true) +
                             '&saveDraft=' + encodeURIComponent(true) +
                             '&isManualSave=' +
                             encodeURIComponent(isManualSave) +
                             '&isShallowSave=' +
                             encodeURIComponent(isShallowSave) +
                             '&timezoneOffset=' +
                             encodeURIComponent(timezoneOffset);

  // Add form input and post body.
  for (var i in dirtyData) {
    var name = i;
    var value = dirtyData[i];

    formattedRequestData =
      formattedRequestData + '&' + name + '=' + encodeURIComponent(value);
  }

  return formattedRequestData;
};

 /**
  * Indicates whether or not the entire post should be saved as a normal draft,
  * or if saving just the post body to the item payload is sufficient.
  * @param {Object} dirtyData A map of form input name and value pairs, with
  *     values that may differ from clean data.
  * @return {Boolean} Whether or not the post should be deep or shallow saved.
  */
BLOG_Autosave.prototype.isShallowSave = function(dirtyData) {
  // If no post id, then this is a new post and a deep save is required in
  // order to create a post.
  if (dirtyData['postID'] == '') {
    return false;
  } else {
    // If any data is dirty, besides the post body, then perform a deep save.
    for (var i in dirtyData) {
      if (i == 'postBody') {
        continue;
      } else if (dirtyData[i] != this.cleanData_[i]) {
        return false;
      }
    }
  }

  return true;
};

/**
 * Processes response from xmlhttp request and updates relevants elements on
 * the post form.
 * @param {Object} resp The parsed response in JSON format.
 * @param {Object} dirtyData A map of form input name and value pairs, with
 *     values that may differ from clean data.
 */
BLOG_Autosave.prototype.processResponse = function(resp, formData) {
  var message = resp.message;
  var postID = resp.postID;
  var securityToken = resp.securityToken;

  // Set autosave message and timestamp.
  this.setAutosaveMessage(false, message);

  // Set postID.
  if (postID != null) {
    d('postID').value = postID;
  }

  // Set security token.
  if (securityToken != null) {
    var tokenField = document.stuffform.securityToken;
    tokenField.value = securityToken;
  }

  this.resetCleanData(formData);
  this.touchCount_ = 0;

  // Confirm that no changes were made to form from time the dirty data was
  // collected and the time it was successfully saved.  Then call cleanForm()
  // so user is not prompted with warning about unsaved changes
  // (see formcheck.js)
  if (!this.hasUnsavedChanges(formData)) {
    cleanForm();
  }

  // If there are any error messages being displayed, hide them because we are
  // handling an error-free response and then reapply the IE layout kludge.
  d('errormsgdiv').innerHTML = '';
  hideElement(d('statusmsg'));
  hideElement(d('postDateTimeMsgDiv'));
  NestlingFormFields();
};

/**
 * Shows error returned in response in place of autosave timestamp.
 * @param {Object} resp The parsed response in JSON format.
 */
BLOG_Autosave.prototype.showError = function(resp) {
  var isPublished = resp.isPublished;
  if (resp.error != null) {
    var error = parseInt(resp.error, 10);
    var errorHtml = '';
    for (var i = 0; i < error; i++) {
      errorHtml += '<p class="errormsg">' + resp['error' + i] + '</p>';
    }
    // replace all current error messages with these new ones and unhide the
    // top error div
    d('errormsgdiv').innerHTML = errorHtml;
    showElement(d('statusmsg'));
  }

  this.setAutosaveMessage(true, autosave_failed_message);

  var autosaveButton = d('autosaveButton');
  var saveButton = d('saveButton');

  // If draft changed to published, hide and disable autosaveButton, and display
  // saveButton.  Otherwise, swap autosaveButton with saveButton to submit form,
  // and restart recurring autosaves.
  if (isPublished) {
    autosaveButton.onclick = null;
    hideElement(autosaveButton);
    showElement(saveButton);
  } else {
    this.start();
  }
};

/**
 * Sets the autosave message text.
 * @param {String} message
 */
BLOG_Autosave.prototype.setAutosaveMessage = function(isError, message) {
  var el = d('autosaveMessage');
  el.className = isError ? 'autosave-message-error' : 'autosave-message-normal';
  if (el.firstChild == null) {
    el.appendChild(document.createTextNode(message));
  } else {
    el.firstChild.nodeValue = message;
  }
};

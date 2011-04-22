/**
 * Generates the dom for the dialog box and opaque click-trapping div.
 */
function BLOG_generateDialog(locationEl, centerOnLocation) {
  var modalDiv = d('dialogBox');

  // If there is already a dialogBox, reuse it.
  if (!modalDiv) {
    modalDiv = document.createElement('div');
    modalDiv.id = 'dialogBox';
  }

  modalDiv.style.zIndex = '1001';
  modalDiv.style.position = 'absolute';
  modalDiv.style.display = 'none';

  var locationPos = getXY(locationEl);
  modalDiv.style.top = locationPos.y + 20 + 'px';

  if (centerOnLocation) {
    modalDiv.style.width = locationEl.clientWidth + 'px';
    modalDiv.style.left = locationPos.x + 'px';
  } else {
    modalDiv.style.width = '100%';
  }

  // The modal div is placed in the body alongside the opaque div. This is
  // a workaround for an IE bug where placing the div in any positioned element
  // would cause the opaque div to be shown over it.
  document.body.appendChild(modalDiv);

  // set click trapping opaque div
  var opaqueDiv = BLOG_createOpaqueClickTrappingDiv(document);
  document.body.appendChild(opaqueDiv);
}

function BLOG_makeDialog(domNode, locationEl, centerOnLocation) {
  BLOG_generateDialog(locationEl, centerOnLocation);

  var dialogBox = d('dialogBox');
  dialogBox.innerHTML = '';
  dialogBox.appendChild(domNode);
  dialogBox.style.display = 'block';
}

/**
 * Function that requests a page from the server and retrieves the dialog box
 * via xmlHttp
 *
 * @param url The location of the dialog box.
 * @param params The params to pass to the dialog, null if it's a GET.
 * @param locationEl The element to position the dialog over.
 * @param centerOnLocation If true, centers on the location. Otherwise centers
 *    in the window. Should only be true if the locationEl is fixed-width.
 * @param opt_callback Optional JS callback that gets called after the dialog is
 *    loaded.
 * @return false cancels event propagation.
 */
function BLOG_retrieveDialog(url, params, locationEl, centerOnLocation,
                             opt_callback) {
  BLOG_generateDialog(locationEl, centerOnLocation);

  // IE likes to cache XmlHttpRequests. Prevent that.
  params[params.length] = 'zx';
  params[params.length] =
      Math.floor(Math.random() * Math.pow(2, 32)).toString(36);

  Goo_SendMessage(url, params, undefined,
      function(req) {
        BLOG_showDialog(req);
        if (opt_callback) {
          opt_callback();
        }
        return false;
      });

  // returning false cancels event propagation
  return false;
}

/**
 * Callback function that injects the HTML for the dialog box into
 * a div named dialogBox.
 */
function BLOG_showDialog(req) {
  if (req.status != 200) {
    BLOG_cancelDialog(false);
    return false;
  }

  var dialogBox = d('dialogBox');
  dialogBox.innerHTML = req.responseText;
  dialogBox.style.display = 'block';

  // return false to prevent the response from being JS eval'd by
  // Goo_SendMessage's response handler
  return false;
}

/**
 * Hides the dialog box again.
 *
 * @param removeDialog whether or not we should remove the dialog on cancel.
 *        otherwise, we just hide it (e.g. if we are submitting an iframe in
 *        the dialog).
 */
function BLOG_cancelDialog(removeDialog) {
  var dialogBox = document.getElementById('dialogBox');
  var opaqueDiv = document.getElementById('clickTrappingDiv');
  if (opaqueDiv) {
    opaqueDiv.parentNode.removeChild(opaqueDiv);
  }

  if (removeDialog) {
    dialogBox.parentNode.removeChild(dialogBox);
  } else {
    dialogBox.style.display = 'none';
  }

  if (window.removeEventListener) {
    window.removeEventListener('resize', BLOG_resizeOpaqueClickTrappingDiv,
        true);
  } else if (window.detachEvent) {
    window.detachEvent('onresize', BLOG_resizeOpaqueClickTrappingDiv);
  }

  // returning false cancels event propagation
  return false;
}

/**
 * If the dialog box was hidden using BLOG_cancelDialog(true), then this
 * method redisplays the dialog box.
 *
 * @return whether or not the dialog box was displayed.
 */
function BLOG_redisplayDialog() {
  var dialogBox = document.getElementById('dialogBox');
  if (dialogBox) {
    dialogBox.style.display = 'block';

    var opaqueDiv = BLOG_createOpaqueClickTrappingDiv(document);
    document.body.appendChild(opaqueDiv);
    return true;
  }
  return false;
}

function BLOG_createOpaqueClickTrappingDiv(parentDoc) {
  var opaqueDiv = document.getElementById('clickTrappingDiv');

  // makes us resiliant to being called when a dialog is already visible
  if (opaqueDiv != null) {
    return opaqueDiv;
  }

  opaqueDiv = parentDoc.createElement('div');
  opaqueDiv.id = 'clickTrappingDiv';
  opaqueDiv.style.position = 'absolute';
  opaqueDiv.style.top = '0';
  opaqueDiv.style.left = '0';
  opaqueDiv.style.width = '100%';
  opaqueDiv.style.height = document.body.scrollHeight + 'px';
  opaqueDiv.style.zIndex = '1000';
  opaqueDiv.style.cursor = 'default';
  opaqueDiv.style.background = 'white';
  // Opacity for IE
  opaqueDiv.style.filter = 'alpha(opacity=50)';
  // Opacity for other browsers
  opaqueDiv.style.opacity = '.5';
  opaqueDiv.innerHTML = '&nbsp;';

  if (window.addEventListener) {
    window.addEventListener('resize', BLOG_resizeOpaqueClickTrappingDiv, true);
  } else if (window.attachEvent) {
    window.attachEvent('onresize', BLOG_resizeOpaqueClickTrappingDiv);
  }

  return opaqueDiv;
}

function BLOG_resizeOpaqueClickTrappingDiv() {
  var opaqueDiv = document.getElementById('clickTrappingDiv');
  if (opaqueDiv) {
    opaqueDiv.style.height = document.body.scrollHeight + 'px';
  }
}

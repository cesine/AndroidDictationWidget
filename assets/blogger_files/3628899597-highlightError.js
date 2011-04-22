function highlightError(errBegin, errEnd, elemID) {
  var errorLoc = document.getElementById(elemID);
  if (errorLoc == null) {
    return;
  }

  if (errorLoc.setSelectionRange) {
    errorLoc.focus();
    errorLoc.setSelectionRange(errBegin, errEnd + 1);
  }
  else if (errorLoc.createTextRange) {
    var range = errorLoc.createTextRange();
    range.collapse(true);
    range.moveEnd('character', errEnd + 1);
    range.moveStart('character', errBegin);
    range.select();
  }
}

var w_a=true,w_=null,w_b=false,w_c=this;if(!Function.prototype.apply)Function.prototype.apply=function(a,b){var c=[];a||(a=w_c);for(var d=b||[],e=0;e<d.length;e++)c[e]="args["+e+"]";c="oScope.__applyTemp__.peek()("+c.join(",")+");";if(!a.__applyTemp__)a.__applyTemp__=[];a.__applyTemp__.push(this);c=eval(c);a.__applyTemp__.pop();return c};if(!Array.prototype.push)Array.prototype.push=function(){for(var a=0;a<arguments.length;a++)this[this.length]=arguments[a];return this.length};
if(!Array.prototype.pop)Array.prototype.pop=function(){if(this.length){var a=this[this.length-1];this.length--;return a}};Array.prototype.peek=function(){return this[this.length-1]};if(!Array.prototype.shift)Array.prototype.shift=function(){if(this.length!=0){for(var a=this[0],b=0;b<this.length-1;b++)this[b]=this[b+1];this.length--;return a}};
if(!Array.prototype.unshift)Array.prototype.unshift=function(){for(var a=arguments.length,b=this.length-1;b>=0;b--)this[b+a]=this[b];for(b=0;b<a;b++)this[b]=arguments[b];return this.length};if(!Array.prototype.forEach)Array.prototype.forEach=function(a,b){for(var c=0;c<this.length;c++)a.call(b,this[c],c,this)};
function w_d(a,b){var c=a.k||[];c=c.concat(Array.prototype.slice.call(arguments,2));if(typeof a.i!="undefined")b=a.i;if(typeof a.g!="undefined")a=a.g;var d=function(){var e=c.concat(Array.prototype.slice.call(arguments));return a.apply(b,e)};d.k=c;d.i=b;d.g=a;return d}Function.prototype.bind=function(a){return w_d.apply(w_,[this,a].concat(Array.prototype.slice.call(arguments,1)))};Function.prototype.inherits=function(a){var b=function(){};this.l=b.prototype=a.prototype;this.prototype=new b};var w_e=w_b;function w_f(a){w_g(a,0)}function w_h(a,b){var c="Javascript exception: "+(b?b:"")+" "+a;if(w_i())c+=" "+a.name+": "+a.message+" ("+a.number+")";var d="";if(typeof a=="string")d=a+"\n";else for(var e in a)try{d+=e+": "+a[e]+"\n"}catch(f){}d+=w_j(w_h.caller);w_g(c+"\n"+d,1)}var w_k=/function (\w+)/;function w_l(a){if(a=w_k.exec(String(a)))return a[1];return""}
function w_j(a){try{if(!w_i()&&!(w_m("safari")||w_m("konqueror"))&&w_m("mozilla"))return Error().stack;if(!a)return"";for(var b="- "+w_l(a)+"(",c=0;c<a.arguments.length;c++){if(c>0)b+=", ";var d=String(a.arguments[c]);if(d.length>40)d=d.substr(0,40)+"...";b+=d}b+=")\n";b+=w_j(a.caller);return b}catch(e){return"[Cannot get stack trace]: "+e+"\n"}}var w_n,w_o=w_,w_p=w_b;
function w_q(){if((w_o==w_||w_o.closed)&&!w_p)try{w_p=w_a;w_o=window.open("","debug","width=700,height=500,toolbar=no,resizable=yes,scrollbars=yes,left=16,top=16,screenx=16,screeny=16");w_o.blur();w_o.document.open();w_p=w_b;var a="<font color=#ff0000><b>To turn off this debugging window,hit 'D' inside the main caribou window, then close this window.</b></font><br>";w_r(a)}catch(b){}}
function w_g(a,b){if(w_e){try{var c=(new Date).getTime()-w_n,d="["+c+"] "+w_s(a).replace(/\n/g,"<br>")+"<br>";if(b==1){d="<font color=#ff0000><b>Error: "+d+"</b></font>";w_o.focus()}}catch(e){}w_r(d)}else typeof w_t!="undefined"&&w_t(w_s(a))}function w_r(a){if(w_e)try{w_q();w_o.document.write(a);w_o.scrollTo(0,1E6)}catch(b){}};function w_m(a){if(a in w_u)return w_u[a];return w_u[a]=navigator.userAgent.toLowerCase().indexOf(a)!=-1}var w_u={};function w_i(){return w_m("msie")&&!window.opera}
var w_v={b:function(a){return a.document.body.scrollTop},c:function(a){return a.document.documentElement.scrollTop},a:function(a){return a.pageYOffset}},w_w={b:function(a){return a.document.body.scrollLeft},c:function(a){return a.document.documentElement.scrollLeft},a:function(a){return a.pageXOffset}},w_x={b:function(a){return a.document.body.clientWidth},c:function(a){return a.document.documentElement.clientWidth},a:function(a){return a.innerWidth}},w_y={b:function(a){return a.document.body.clientHeight},
c:function(a){return a.document.documentElement.clientHeight},a:function(a){return a.innerHeight}};function w_z(a,b){try{if(w_m("safari")||w_m("konqueror"))return b.a(a);else if(!window.opera&&"compatMode"in a.document&&a.document.compatMode=="CSS1Compat")return b.c(a);else if(w_i())return b.b(a)}catch(c){}return b.a(a)}var w_A=/&/g,w_B=/</g,w_C=/>/g;function w_s(a){if(!a)return"";return a.replace(w_A,"&amp;").replace(w_B,"&lt;").replace(w_C,"&gt;").replace(w_D,"&quot;")}var w_D=/\"/g;
function w_E(a,b){try{if(w_F(b.selectionEnd))return b.selectionEnd;else if(a.document.selection&&a.document.selection.createRange){var c=a.document.selection.createRange();if(c.parentElement()!=b)return-1;var d=c.duplicate();d.moveToElementText(b);d.setEndPoint("EndToStart",c);var e=d.text.length;if(e>b.value.length)return-1;return e}else{w_f("Unable to get cursor position for: "+navigator.userAgent);return b.value.length}}catch(f){w_h(f,"Cannot get cursor pos")}return-1}
function w_G(a,b,c){if(w_F(b.selectionEnd)&&w_F(b.selectionStart)){b.selectionStart=c;b.selectionEnd=c}else if(a.document.selection&&b.createTextRange){a=b.createTextRange();a.collapse(w_a);a.move("character",c);a.select()}}function w_F(a){return typeof a!="undefined"}function w_H(a){var b;if(a.keyCode)b=a.keyCode;else if(a.which)b=a.which;return b}function w_I(a){return document.getElementById(a)}function w_J(a){return document.all[a]}var w_K=document.getElementById?w_I:w_J;
function w_t(a){try{if(window.parent!=window&&window.parent.log){window.parent.log(window.name+"::"+a);return}}catch(b){}var c=w_K("log");if(c){a="<p class=logentry><span class=logdate>"+new Date+"</span><span class=logmsg>"+a+"</span></p>";c.innerHTML=a+c.innerHTML}else window.status=a};function w_L(a,b,c){this.x=a;this.y=b;this.coordinateFrame=c||w_}w_L.prototype.toString=function(){return"[P "+this.x+","+this.y+"]"};function w_M(a,b){this.dx=a;this.dy=b}w_M.prototype.toString=function(){return"[D "+this.dx+","+this.dy+"]"};function w_N(a,b,c,d,e){this.x=a;this.y=b;this.w=c;this.h=d;this.coordinateFrame=e||w_}w_N.prototype.contains=function(a){return this.x<=a.x&&a.x<this.x+this.w&&this.y<=a.y&&a.y<this.y+this.h};
w_N.prototype.toString=function(){return"[R "+this.w+"x"+this.h+"+"+this.x+"+"+this.y+"]"};function w_O(a){function b(h){for(var g=a.offsetParent;g&&g.offsetParent;g=g.offsetParent){if(g.scrollLeft)h.x-=g.scrollLeft;if(g.scrollTop)h.y-=g.scrollTop}}if(!a)return w_;var c;c=a.ownerDocument&&a.ownerDocument.parentWindow?a.ownerDocument.parentWindow:a.ownerDocument&&a.ownerDocument.defaultView?a.ownerDocument.defaultView:window;if(a.getBoundingClientRect){var d=a.getBoundingClientRect();return new w_N(d.left+w_z(c,w_w),d.top+w_z(c,w_v),d.right-d.left,d.bottom-d.top,c)}if(a.ownerDocument&&a.ownerDocument.getBoxObjectFor){d=
a.ownerDocument.getBoxObjectFor(a);c=new w_N(d.x,d.y,d.width,d.height,c);b(c);return c}for(var e=d=0,f=a;f.offsetParent;f=f.offsetParent){d+=f.offsetLeft;e+=f.offsetTop}c=new w_N(d,e,a.offsetWidth,a.offsetHeight,c);b(c);return c};function _ac_install(){w_P(document.body,"onkeydown",w_Q);w_P(document.body,"onkeypress",w_Q)}function _ac_register(a){for(var b=w_R.length;--b>=0;)if(w_R[b]===a)return;w_R.push(a)}function _ac_onfocus(a){w_Q(a)}function _ac_isCompleting(){return!!w_S&&!w_T}function _ac_isCompleteListShowing(){var a=document.getElementById("ac-list");return!!w_S&&!w_T&&w_U&&w_U.length&&a&&a.innerHTML}function _ac_cancel(){w_T=w_a;w_V(w_b)}function w_P(a,b,c){var d=a[b];a[b]=d?w_W(a[b],c):c;return d}
function w_X(a){if("stopPropagation"in a)a.stopPropagation();else a.cancelBubble=w_a;"preventDefault"in a&&a.preventDefault()}function w_W(a,b){return function(){var c=a.apply(this,arguments),d=b.apply(this,arguments);return c===w_b||d===w_b?w_b:w_a}}
function w_Q(a){a=a||window.event;var b=a.target||a.srcElement;if("INPUT"==b.tagName&&b.type.match(/^text$/i)||"TEXTAREA"==b.tagName){var c=w_H(a),d=a.type=="keydown",e=a.shiftKey;if(b!==w_Y||w_S===w_){w_Y=b;var f=w_b;if(13!==c&&27!==c){for(var h=0;h<w_R.length;++h){var g=w_R[h](b,a);if(g){w_S=g;w_Z=w_P(w_Y,"onblur",_ac_ob);f=w_a;break}}if(!f){w_Y=w_;_ac_ob(w_)}}}if(w_S){b=w_S.e(c,d,e);f=w_U&&w_U.length>0;h=w_b;if(b&&f){h=!w_T&&!!w_U&&w__>=0;window.setTimeout(function(){w_S&&w_0(c,d,e)},0)}else if(!b){h=
_ac_isCompleteListShowing()&&(c==27||!e&&c==40||!e&&c==38);window.setTimeout(function(){w_S&&w_0(c,d,e)},0)}h&&w_X(a);return!h}}return w_a}function _ac_ob(){window.setTimeout(function(){if(w_Y)w_Y.onblur=w_Z;w_Z=w_Y=w_S=w_;w_T=w_b;w_V(w_b)},0)}function _AC_Store(){}_AC_Store.prototype.completable=function(){alert("UNIMPLEMENTED completable")};_AC_Store.prototype.completions=function(){alert("UNIMPLEMENTED completions")};_AC_Store.prototype.substitute=function(){alert("UNIMPLEMENTED substitute")};
_AC_Store.prototype.j=w_a;_AC_Store.prototype.f=1;_AC_Store.prototype.e=function(a,b,c){if(!b&&(13===a||w_1==a&&this.j))return w_a;if(9===a&&!c)return b==w_i();return w_b};
function _AC_SimpleStore(a){this.d={};for(var b=RegExp("[\\t-\\r -#%-/:;?@\\x5b-\\x5d_{}\u00a0\u00a1\u00ab\u00b7\u00bb\u00bf\\u037e\\u0387\\u055a-\\u055f\\u0589\\u058a\\u05be\\u05c0\\u05c3\\u05f3\\u05f4\\u060c\\u060d\\u061b\\u061f\\u066a-\\u066d\\u06d4\\u0700-\\u070d\\u0964\\u0965\\u0970\\u0df4\\u0e4f\\u0e5a\\u0e5b\\u0f04-\\u0f12\\u0f3a-\\u0f3d\\u0f85\\u104a-\\u104f\\u10fb\\u1361-\\u1368\\u166d\\u166e\\u1680\\u169b\\u169c\\u16eb-\\u16ed\\u1735\\u1736\\u17d4-\\u17d6\\u17d8-\\u17da\\u1800-\\u180a\\u180e\\u1944\\u1945\\u2000-\\u200b\\u2010-\\u2029\\u202f-\\u2043\\u2045-\\u2051\\u2053\\u2054\\u2057\\u205f\\u207d\\u207e\\u208d\\u208e\\u2329\\u232a\\u23b4-\\u23b6\\u2768-\\u2775\\u27e6-\\u27eb\\u2983-\\u2998\\u29d8-\\u29db\\u29fc\\u29fd\\u3000-\\u3003\\u3008-\\u3011\\u3014-\\u301f\\u3030\\u303d\\u30a0\\u30fb\\ufd3e\\ufd3f\\ufe30-\\ufe52\\ufe54-\\ufe61\\ufe63\\ufe68\\ufe6a\\ufe6b\\uff01-\\uff03\\uff05-\\uff0a\\uff0c-\\uff0f\\uff1a\\uff1b\\uff1f\\uff20\\uff3b-\\uff3d\\uff3f\\uff5b\\uff5d\\uff5f-\\uff65]+"),c=
0;c<a.length;++c){var d=a[c];if(d)for(var e=d.split(b),f=0;f<e.length;++f)if(e[f]){var h=e[f].charAt(0).toLowerCase(),g=this.d[h];if(g){if(g[g.length-1].value==d)continue}else g=this.d[h]=[];g.push(new _AC_Completion(d,w_))}}this.countThreshold=10}_AC_SimpleStore.inherits(_AC_Store);
_AC_SimpleStore.prototype.completable=function(a,b){for(var c=0,d=0,e=0;e<b;++e){var f=a.charAt(e);switch(d){case 0:if('"'==f)d=1;else if(","==f)c=e+1;break;case 1:if('"'==f)d=0}}for(;c<b&&" \t\r\n".indexOf(a.charAt(c))>=0;)++c;return a.substring(c,b)};
_AC_SimpleStore.prototype.completions=function(a,b){if(!a)return[];var c=RegExp("^(.*[\\s<\"',])?("+a.replace(/([\^*+\-\$\\\{\}\(\)\[\]\#?\.])/g,"\\$1")+")(.*)","i");if(!(b&&b.length)&&a)b=this.d[a.charAt(0).toLowerCase()];var d=[];if(b)for(var e=0;e<b.length;++e){var f=b[e].value.match(c);if(f){d.push(new _AC_Completion(b[e].value,w_2(f[1]||"")+"<b>"+w_2(f[2])+"</b>"+w_2(f[3])));if(d.length>this.countThreshold)break}}d.sort(_AC_CompareACCompletion);return d};
function _AC_CompareACCompletion(a,b){var c=a.value.toLowerCase().replace(/^\W*/,""),d=b.value.toLowerCase().replace(/^\W*/,"");return a.value===b.value?0:c<d?-1:1}_AC_SimpleStore.prototype.substitute=function(a,b,c,d){return a.substring(0,b-c.length)+d.value+", "+a.substring(b)};function _AC_Completion(a,b){this.value=a;this.html=b}_AC_Completion.prototype.toString=function(){return"(AC_Completion: "+this.value+")"};var w_R=[],w_Y=w_,w_S=w_,w_Z=w_,w_T=w_b,w_3=w_,w_U=w_,w__=-1;
function w_0(a,b,c){var d=a===37||a===39,e=w_4(w_Y)===w_Y.value.length;if(!d||_ac_isCompleteListShowing()||a===39&&e)w_5();d=w_a;e=w_U?w_U.length:0;if(w_S.e(a,b,c))w__>=0&&w_6();else switch(a){case 27:w__=-1;d=w_b;break;case 38:if(b)w__=Math.max(e>=0?0:-1,w__-1);break;case 40:if(b)w__=Math.min(e-1,w__+1)}w_Y&&w_V(d,w_S.f)}function _ac_select(a){w__=a;w_6();w_5();w_V(w_a,w_S.f)}
function w_6(){var a=w_4(w_Y);w_Y.value=w_S.substitute(w_Y.value,a,w_3,w_U[w__]);w__=-1;w_3=w_U=w_;w_G(window,w_Y,w_Y.value.length)}function w_5(){if(w_T){w_U=w_3=w_;w__=-1}else{var a=w_4(w_Y);a=w_S.completable(w_Y.value,a);if(a!=w_3){var b;w_U=w_;w__=-1;var c=w__>=0?w_U[w__].value:w_;w_U=w_S.completions(a,b);w__=-1;for(b=0;b<w_U.length;++b)if(c==w_U[b].value){w__=b;break}w_3=a}}}
function w_V(a,b){if(b===undefined)b=1;var c=document.getElementById("ac-list");if(a&&w_U&&w_U.length){if(!c){c=document.createElement("DIV");c.id="ac-list";c.style.position="absolute";c.style.display="none";document.body.appendChild(c)}if(w__<0)w__=0;for(var d=[],e=0;e<w_U.length;++e)d.push('<div onmousedown="try{_ac_select(',e,')}finally{return false}"',e==w__?" class=selected>":">",w_U[e].html,"</div>");c.innerHTML=d.join("");d=w_O(w_Y);c.style.left=d.x+"px";c.style.top=d.y+d.h+"px";c.style.display=
"";e=w_O(c);if(b&1){var f=w_z(window,w_y);if(e.y+e.h>f)c.style.top=d.y-e.h+"px";f=w_z(window,w_x);if(e.x+e.w>f)c.style.left=d.x+d.w-e.w+"px"}}else if(c){c.style.display="none";c.innerHTML=""}}function w_2(a){return a.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/\"/g,"&quot;").replace(/ /g,"&nbsp;").replace(/\r\n?|\n/g,"<br>")}
function w_4(a){if("INPUT"==a.tagName){var b=a.value.length;if(undefined!=a.selectionStart)b=a.selectionStart;else if(document.selection){a=document.selection.createRange();a.moveStart("character",-b);b=a.text.length}return b}else return w_E(window,a)}var w_1=",".charCodeAt(0);

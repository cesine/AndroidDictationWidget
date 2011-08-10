var labelType, useGradients, nativeTextSupport, animate;

(function() {
  var ua = navigator.userAgent,
      iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),
      typeOfCanvas = typeof HTMLCanvasElement,
      nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
      textSupport = nativeCanvasSupport 
        && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');
  //I'm setting this based on the fact that ExCanvas provides text support for IE
  //and that as of today iPhone/iPad current text support is lame
  labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';
  nativeTextSupport = labelType == 'Native';
  useGradients = nativeCanvasSupport;
  animate = !(iStuff || !nativeCanvasSupport);
})();

var Log = {
  elem: false,
  write: function(text){
    if (!this.elem) 
      this.elem = document.getElementById('log');
    this.elem.innerHTML = text;
    this.elem.style.left = (200 - this.elem.offsetWidth / 2) + 'px';
  }
};



function init(){
    //init data
json = {id: "1",
name: "Root",
hidden: "0",
data: { content:"empty"},
children: [{
id: "2",
name: "About me",
hidden: "0",
data: { content:"Your blog&apos;s About Me page should not be overlooked. It&apos;s an essential tool to establish who you are as a blogger and help readers understand what your blog is about. Simply listing your name and contact information is not enough. Sell yourself and your blog on your About Me page, and make readers believe you&apos;re not only an expert in your blog&apos;s topic but your blog is also the place for people to find information about your topic on the web. From &lt;a href=&apos;http://weblogs.about.com/od/partsofablog/qt/AboutPage.htm&apos;&gt;About.com&lt;/a&gt;"},
children: [{
id: "3",
name: "About my blog",
hidden: "0",
data: { content:"&lt;p&gt;Your blog&apos;s About Me page should not be overlooked. It&apos;s an essential tool to establish who you are as a blogger and help readers understand what your blog is about. Simply listing your name and contact information is not enough. Sell yourself and your blog on your About Me page, and make readers believe you&apos;re not only an expert in your blog&apos;s topic but your blog is also the place for people to find information about your topic on the web. From &lt;a href=&apos;http://weblogs.about.com/od/partsofablog/qt/AboutPage.htm&apos;&gt;About.com&lt;/a&gt;&lt;/p&gt;Following are the three most important elements to include on your About Me page:Your experience and why you&apos;re the best person to blog about your subject matterLinks to your other websites or blogs self promotion is critical to your success as a bloggerYour contact information so interested readers can ask questions or reach out to you for other business opportunities (which happens often in the blogosphere)"},
children: []
} ]
} ,{
id: "4",
name: "How to use the drafts tree",
hidden: "0",
data: { content:"The drafts tree lets you visualize/listen to blog entries, and re-draft or re-wind until you are ready to publish. Each time you hit the save button, a new node is created. "},
children: [{
id: "5",
name: "Use via recording...",
hidden: "0",
data: { content:"You can brainstorm and build your blog entry by recording your voice, and listening to it. Once you have a blog entry that you are satisfied with you can send it for transcription. Then you can edit it in text form and publish it. Alternatively, you can use AuBlog as a traditional blog client by just typing in the Edit Entry page."},
children: []
} ,{
id: "6",
name: "Use via typing...",
hidden: "0",
data: { content:"You can also simply type your blog entry by clicking on the Edit button."},
children: []
} ]
} ]
};
    //end
    //init Spacetree
    //Create a new ST instance
    var st = new $jit.ST({
    	orientation: "top",
    	indent:10,
        //id of viz container element
        injectInto: 'infovis',
        //set duration for the animation
        duration: 800,
        //set animation transition type
        transition: $jit.Trans.Quart.easeInOut,
        //set distance between node and its children
        levelDistance: 50,
        //enable panning
        Navigation: {
          enable:true,
          panning:true
        },
        //set node and edge styles
        //set overridable=true for styling individual
        //nodes or edges
        Node: {
            height: 30,
            width: 40,
            type: 'ellipse',
            color: '#aaa',
            overridable: true
        },
        
        Edge: {
            type: 'bezier',
            overridable: true
        },
        
        onBeforeCompute: function(node){
            Log.write("loading " + node.name);
        },
        
        onAfterCompute: function(node){
            Log.write("<input type='button' value='Edit "+node.name+"' onClick='editId("+node.id+")'/><br /><input type='button' value='Delete "+node.name+"' onClick='deleteId("+node.id+")'/>");
        },
        
        //This method is called on DOM label creation.
        //Use this method to add event handlers and styles to
        //your node.
        onCreateLabel: function(label, node){
            label.id = node.id;            
            label.innerHTML = node.name;
            label.onclick = function(){
            	//if(normal.checked) {
            	  st.onClick(node.id);
            	//} else {
                //st.setRoot(node.id, 'animate');
            	//}
            };
            //set label styles
            var style = label.style;
            style.width = 40 + 'px';
            style.height = 17 + 'px';            
            style.cursor = 'pointer';
            style.color = '#333';
            style.fontSize = '0.8em';
            style.textAlign= 'center';
            style.paddingTop = '8px';
        },
        
        //This method is called right before plotting
        //a node. It's useful for changing an individual node
        //style properties before plotting it.
        //The data properties prefixed with a dollar
        //sign will override the global node style properties.
        onBeforePlotNode: function(node){
            //add some color to the nodes in the path between the
            //root node and the selected node.
            if (node.selected) {
                node.data.$color = "#ff7";
            }
            else {
                delete node.data.$color;
                //if the node belongs to the last plotted level
                if(!node.anySubnode("exist")) {
                    //count children number
                    var count = 0;
                    node.eachSubnode(function(n) { count++; });
                    //assign a node color based on
                    //how many children it has
                    node.data.$color = ['#aff', '#aee', '#add', '#acc', '#abb', '#acb'][count];                    
                }
            }
        },
        
        //This method is called right before plotting
        //an edge. It's useful for changing an individual edge
        //style properties before plotting it.
        //Edge data proprties prefixed with a dollar sign will
        //override the Edge global style properties.
        onBeforePlotLine: function(adj){
            if (adj.nodeFrom.selected && adj.nodeTo.selected) {
                adj.data.$color = "#eed";
                adj.data.$lineWidth = 3;
            }
            else {
                delete adj.data.$color;
                delete adj.data.$lineWidth;
            }
        }
    });
    //load json data
    st.loadJSON(json);

    //compute node positions and layout
    st.compute();
    //optional: make a translation of the tree
    st.geom.translate(new $jit.Complex(-200, 0), "current");
    //emulate a click on the root node.
    st.onClick(st.root);
    //end
    
    //Add event handlers to switch spacetree orientation.
    var top = $jit.id('r-top'), 
        left = $jit.id('r-left'), 
        bottom = $jit.id('r-bottom'), 
        right = $jit.id('r-right'),
        normal = $jit.id('s-normal');
        
    
    function changeHandler() {
        if(this.checked) {
             bottom.disabled = right.disabled = left.disabled = top.disabled = true;
            st.switchPosition(this.value, "animate", {
                onComplete: function(){
                    bottom.disabled = right.disabled = left.disabled = top.disabled = false;
                }
            });
        }
    };
    
    top.onchange = left.onchange = bottom.onchange = right.onchange = changeHandler;
    //end

}

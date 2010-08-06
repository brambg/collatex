[#ftl]
[@c.page title="REST service result" head='
<base url="http://localhost:8080"/>
<link type="text/css" href="http://localhost:8080${ctx}/css/graphvisualisation.css" rel="stylesheet" />
<script language="javascript" type="text/javascript" src="http://localhost:8080${ctx}/js/jit-yc.js"></script>
<script language="javascript" type="text/javascript" src="http://localhost:8080${ctx}/js/graphvisualisation.js"></script>
']

<h1>REST service result</h1>

<p>	
[#list graph.witnesses as w]
	<strong>${w.sigil}</strong> : ${w.content}<br/>
[/#list]
</p>

<div id="container"> 
  <div id="left-container"> 
    <div id="id-list"></div> 
  </div> 
 
  <div id="center-container"> 
    <div id="infovis"></div>    
  </div> 
 
  <div id="right-container"> 
    <div id="inner-details"></div> 
  </div> 
 
  <div id="log"></div> 
</div> 
	
<script language="javascript" type="text/javascript">
  showForcedDirected(${graph.json});
</script>

[/@c.page]
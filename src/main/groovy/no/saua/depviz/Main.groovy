import groovy.json.JsonOutput

import static no.saua.depviz.dependencyfinder.DependencyFinder.find
Map<String, Set<String>> dependencies = find("net.chrisrichardson.eventstore",
        '/home/knut/tmp/event-sourcing-examples/java-spring/common-web/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/common-backend/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/accounts-command-side-backend/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/transactions-command-side-backend/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/accounts-command-side-web/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/transactions-command-side-web/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/accounts-query-side-backend/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/accounts-query-side-web/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/monolithic-service/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/accounts-command-side-service/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/accounts-query-side-service/build/classes/main',
        '/home/knut/tmp/event-sourcing-examples/java-spring/transactions-command-side-service/build/classes/main',
)

// :: Convert dependencies to nodes and graphs
// Give each class an id
def allClassesFound = (dependencies.keySet() + dependencies.values().flatten())
Map<String, Integer> nodeIds = allClassesFound.withIndex().inject([:]) { list, classNameAndIndex ->
    list[classNameAndIndex[0]] = classNameAndIndex[1]
    return list
}

def simpleClassName = { String className ->
    return className.split('\\.').last()
}

def packageName = { String className ->
    int lastIndex = className.lastIndexOf(".")
    return className.subSequence(0, lastIndex)
}

// visjs nodes
def nodes = allClassesFound.collect { className ->
    [id: nodeIds.get(className),
     title: simpleClassName(className),
     group: packageName(className),
     level: className.count(".")]
}

// visjs edges
def edges = dependencies.collect { className, deps ->
    deps.collect { dep ->
        [from: nodeIds.get(className), to: nodeIds.get(dep)]
    }
}.flatten()

new File("depviz.html").withWriter { out ->
    out.print """<!doctype html>
<html>
<head>
<title>Network</title>

    <script type="text/javascript" src="http://visjs.org/dist/vis.js"></script>
<link href="http://visjs.org/dist/vis.css" rel="stylesheet" type="text/css" />

<style type="text/css">
html, body, #mynetwork {
    margin: 0;
    padding: 0;
    width: 100%;
    height: 100%;
}
</style>
</head>
<body>

<div id="mynetwork"></div>

<script type="text/javascript">
        // create an array with nodes
        var nodes = new vis.DataSet(${JsonOutput.toJson(nodes)});

// create an array with edges
var edges = new vis.DataSet(${JsonOutput.toJson(edges)});

// create a network
var container = document.getElementById('mynetwork');
var data = {
    nodes: nodes,
    edges: edges
};
var options = {
  edges:{
    arrows: {
      to:     {enabled: true, scaleFactor:1}
    }
  },
  /*layout: {
    randomSeed: undefined,
    hierarchical: {
      enabled:true
      levelSeparation: 150,
      direction: 'UD',   // UD, DU, LR, RL
      sortMethod: 'hubsize' // hubsize, directed
    }
  }*/
}
var network = new vis.Network(container, data, options);
</script>
</body>
</html>
"""
}

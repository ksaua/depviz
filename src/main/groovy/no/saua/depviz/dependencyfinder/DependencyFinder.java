package no.saua.depviz.dependencyfinder;


import classycle.Analyser;
import classycle.ClassAttributes;
import classycle.graph.AtomicVertex;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DependencyFinder {

    public static Map<String, Set<String>> find(String packageFilter, String... rootFolders) throws IOException {
        String[] files = Stream.of(rootFolders)
                .flatMap(rootFolder ->
                        iteratorToStream(Files.fileTreeTraverser().preOrderTraversal(new File(rootFolder))))
                .map(File::getAbsolutePath)
                .collect(Collectors.toList())
                .toArray(new String[0]);

        Analyser analyser = new Analyser(files, t -> true, null, false);
        return Stream.of(analyser.getClassGraph())
                .collect(Collectors.toMap(
                        /* Key is the class name */
                        (AtomicVertex atomicVertex) -> ((ClassAttributes) atomicVertex.getAttributes()).getName(),
                        /* Value is a list of all classes they key is dependent on */
                        (AtomicVertex atomicVertex) ->
                                /* Find all vertexes going out from this class */
                                IntStream.range(0, atomicVertex.getNumberOfOutgoingArcs()).boxed()
                                .map(atomicVertex::getHeadVertex)
                                /* For safety measure, we only want actual classes, not sure if this is needed */
                                .filter(vertex -> vertex.getAttributes() instanceof ClassAttributes)
                                /* Get the class name of the dependency */
                                .map(vertex -> ((ClassAttributes) vertex.getAttributes()).getName())
                                /* Filter out any dependency not inside the package */
                                .filter(name -> name.startsWith(packageFilter))
                                /* Lastly, collect! */
                                .collect(Collectors.toSet())));
    }

    public static <T> Stream<T> iteratorToStream(Iterable<T> iterable) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        iterable.iterator(),
                        Spliterator.ORDERED
                ),
                false
        );
    }
}

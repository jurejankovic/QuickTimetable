package hr.vsite.dipl.quicktimetable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jure on 22.11.2015..
 * This class uses Dijkstra's algorithm to find the shortest path between two stations.
 * In this case shortest means fastest, that is the one that requires the least time to complete.
 * Using tram station ids (type Long) instead of TramStation objects
 */
public class DijkstraRouter {
    private static final String TAG = "DijkstraRouter";

    private List<TramStation> nodes;
    private List<TramLineSegment> edges;
    private Set<Long> unsettledNodes;
    private Set<Long> settledNodes;
    private Map<Long, Long> predecessors;
    private Map<Long, Integer> distance;
    private Long currentLine;
    private boolean lineChanged;
    private List<Long> usedLines;

    private List<Path> paths = new LinkedList<>();

    public DijkstraRouter(TramNetwork network) {
        this.nodes = network.getTramStations();
        this.edges = network.getTramLineSegments();
    }

    // TODO add attributes for path length, current line and direction to path (create Path class?)
    public Path route(List<Long> originIds, List<Long> destinationIds) {
        Path path;
        for (Long originId : originIds) {
            // execute Dijkstra for each origin id
            execute(originId);
            for (Long destinationId : destinationIds) {
                // check each destination id for the origin
                path = getPath(destinationId);
                if (path != null && path.hasMembers()) {
                    // first two stations are used to find path direction
                    path.setDirection(findDirection(path.getMemberAtPos(0), path.getMemberAtPos(1)));
                    paths.add(path);
                }
            }
        }
        if (!paths.isEmpty()) {
            // choose path with shortest distance (quickest path)
            Long minPath = Long.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < paths.size(); i++) {
                // get first element of each path
                Long pathDistance = paths.get(i).getDistance();
                if (pathDistance < minPath) {
                    minPath = pathDistance;
                    minIndex = i;
                }
            }
            return paths.get(minIndex);
        }
        else {
            return null;
        }
    }

    private void execute(Long sourceStation) {
        settledNodes = new HashSet<Long>();
        unsettledNodes = new HashSet<Long>();
        predecessors = new HashMap<Long, Long>();
        distance = new HashMap<Long, Integer>();
        usedLines = new ArrayList<Long>();

        // set start distance to 0
        distance.put(sourceStation, 0);
        // add source station to unsettled list
        unsettledNodes.add(sourceStation);

        currentLine = setInitialLine(sourceStation);

        // while there are unsettled nodes
        while (unsettledNodes.size() > 0) {
            // find the node with minimum distance
            Long node = getMinimum(unsettledNodes);
            settledNodes.add(node);
            unsettledNodes.remove(node);
            findMinimalDistances(node);
            // add current line to list of used lines
            usedLines.add(currentLine);
        }
    }

    /**
     * Sets the initial line by checking the edges that the source station is part of.
     * The first line found is used
     * @param sourceStation
     * @return
     */
    private Long setInitialLine(Long sourceStation) {
        long line = 0;
        for (TramLineSegment edge : edges) {
            if (edge.getOriginId() == sourceStation) {
                line = edge.getLineNumber();
                break;
            }
        }
        return line;
    }

    /**
     * Finds minimal distances for adjacent (neighboring) nodes
     * @param node the node for which min distances are found
     */
    private void findMinimalDistances(Long node) {
        Set<Long> neighbours = getNeighbours(node);
            for (Long neighbour : neighbours) {
                if (getShortestDistance(neighbour) > getShortestDistance(node) + getDistance(node, neighbour)) {
                    distance.put(neighbour, getShortestDistance(node) + getDistance(node, neighbour));
                    predecessors.put(neighbour, node);
                    unsettledNodes.add(neighbour);
                }
            }
        }

    private int getDistance(Long node, Long target) {
        for (TramLineSegment edge : edges) {
                // distance.put(target, getShortestDistance(node) + getDistance(node, target) + penalty);
            if (edge.getOriginId() == node && edge.getDestinationId() == target) {
                if (lineChanged) {
                    lineChanged = false;
                    return edge.getSegmentTime() + 300; // 300 s 5 minutes waiting time
                }
                else {
                    return edge.getSegmentTime();
                }
            }
        }
        throw new RuntimeException();
    }

    private int getShortestDistance(Long destination) {
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        }
        else {
            return d;
        }
    }

    private Set<Long> getNeighbours(Long node) {
        Set<Long> neighbours = new HashSet<Long>();
        // check edges for neighbours
        for (TramLineSegment edge : edges) {
            if (edge.getOriginId() == node && !isSettled(edge.getDestinationId())) {
                neighbours.add(edge.getDestinationId());
                if (edge.getLineNumber() != currentLine) {
                    lineChanged = true;
                    currentLine = edge.getLineNumber();
                }
            }
        }
        return neighbours;
    }


    private Long getMinimum(Set<Long> vertices) {
        Long minimum = null;
        for (Long vertex : vertices) {
            if(minimum == null) {
                minimum = vertex;
            }
            else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }


    private boolean isSettled(Long vertex) {
        return settledNodes.contains(vertex);
    }


    public Path getPath(Long target) {
        Path path = new Path();
        Long step = target;
        Long pathDistance = 0L;

        if (predecessors.get(step) == null) {
            return null;
        }
        path.addMember(step);

        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            // add step distance to total path distance
            pathDistance = pathDistance + distance.get(step);
            path.addMember(step);
        }
        path.setDistance(pathDistance);
        path.setUsedLines(usedLines);
        // reverse the order of path members
        path.reverse();
        return path;
    }

    public int findDirection(Long stationA, Long stationB) {
        int direction = 0;
        for (TramLineSegment edge : edges) {
            if(edge.getOriginId() == stationA && edge.getDestinationId() == stationB) {
                direction = edge.getDirection();
                break;
            }
        }
        return direction;
    }

}

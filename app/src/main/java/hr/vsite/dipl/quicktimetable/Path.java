package hr.vsite.dipl.quicktimetable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** This class represents a path consisting of station id's.
 * Created by Jure on 2.6.2017..
 */

public class Path {
    private long distance;
    private int currentLine;
    private int direction;
    private List<Long> members;
    private List<Long> usedLines;

    public Path() {
        members = new LinkedList<Long>();
        usedLines = new LinkedList<Long>();
    }

    public void addMember(Long member) {
        members.add(member);
    }

    public void reverse() {
        Collections.reverse(members);
    }

    public Long getMemberAtPos(int position) {
        return members.get(position);
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public int getDirection() {
        return direction;
    }


    public void setDirection(int direction) {
        // direction can be 0 or 1
        if (direction == 0 || direction == 1) {
            this.direction = direction;
        }
    }

    public boolean hasMembers() {
        if (members.isEmpty()) {
            return false;
        }
        else {
            return true;
        }
    }

    public List<Long> getMembersList() {
        return members;
    }

    public void setUsedLines(List<Long> usedLines) {
        this.usedLines = usedLines;
    }

    public List<Long> getUsedLines() {
        return usedLines;
    }
}

package xyz.ajisoo.backend.type.rest.apple;

import xyz.ajisoo.backend.type.entity.AppleSession;

public class StatusResponse {

    private int[] visited;
    private boolean complete;

    public StatusResponse() {
        this.visited = new int[0];
        this.complete = false;
    }

    public StatusResponse(AppleSession as) {
        int count = 0;

        if (as.isFound1()) count++;
        if (as.isFound2()) count++;
        if (as.isFound3()) count++;

        this.visited = new int[count];

        int index = 0;

        if (as.isFound1()) this.visited[index++] = 1;
        if (as.isFound2()) this.visited[index++] = 2;
        if (as.isFound3()) this.visited[index++] = 3;

        this.complete = count == 3;
    }

    public int[] getVisited() {
        return visited;
    }

    public void setVisited(int[] visited) {
        this.visited = visited;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
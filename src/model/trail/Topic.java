package model.trail;

public enum Topic {
    MOUNTAIN,
    LAKE,
    RIVER,
    BEACH,
    FOREST;

    public String iconPath() {
        return switch (this) {
            case MOUNTAIN -> "/ui/icons/mountain.png";
            case LAKE     -> "/ui/icons/lake.png";
            case RIVER    -> "/ui/icons/river.png";
            case BEACH    -> "/ui/icons/beach.png";
            case FOREST   -> "/ui/icons/forest.png";
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case MOUNTAIN -> "Mountain Trail";
            case LAKE     -> "Lake Trail";
            case RIVER    -> "River Trail";
            case BEACH    -> "Beach Trail";
            case FOREST   -> "Forest Trail";
        };
    }
}
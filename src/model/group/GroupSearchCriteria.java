package model.group;

import model.trail.Difficulty;
import model.trail.Topic;

/** null means "Any / No restriction". */
public record GroupSearchCriteria(
        Topic topic,              // Topic
        Difficulty maxDifficulty, // Maximum difficulty
        boolean needPetFriendly,  // Bird-watching preference (pet-friendly)
        Double maxVisitHours,     // Maximum visit duration
        Integer joinAsPartySize   // Number of people joining together (used to filter join availability)
) {}



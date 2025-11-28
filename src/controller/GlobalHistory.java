package controller;

import model.animal.Animal;
import service.AnimalHistoryStack;

public class GlobalHistory {

    public static AnimalHistoryStack<Animal> viewedAnimals = new AnimalHistoryStack<>();
}

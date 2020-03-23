import { Difficulty, DisplayMode } from "./creator";
import { IDrawingCreator } from "./creator";


export interface IGameRule {
    solution:       string,
    clues:          string[],
    difficulty:     Difficulty,
    displayMode:    DisplayMode,
    drawing:        IDrawingCreator[],
}
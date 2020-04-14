import { Difficulty, DisplayMode } from "./creator";
import { IDrawingCreator } from "./creator";
import { Side } from "../utils/Side";


export interface IGameRule {
    solution:       string,
    clues:          string[],
    difficulty:     Difficulty,
    displayMode:    DisplayMode,
    side:           Side,
    drawing:        IDrawingCreator[],
}
import { Difficulty } from "./creator";
import { IDrawingCreator } from "./creator";


export interface IGameRule {
    solution:   string,
    clues:      string[],
    difficulty: Difficulty,
    drawing:    IDrawingCreator[],
}
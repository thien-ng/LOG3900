import { Difficulty } from "./creator";
import { IDrawing } from "./creator";


export interface IGameRule {
    solution:   string,
    clues:      string[],
    difficulty: Difficulty,
    drawing:    IDrawing[],
}
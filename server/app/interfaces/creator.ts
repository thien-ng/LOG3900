import { GameMode } from "./game";

export interface ISuggestion {
    drawing: string,
    object: string,
}

export interface ICreateGame {
    gameName:   string,
    solution:   string,
    clues:      string[],
    mode:       GameMode
    // add other informations
}
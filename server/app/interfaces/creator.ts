export interface ISuggestion {
    drawPng: string,
    drawPxl: IDrawingCreator[];
    object:  string,
}

interface ICreateGame {
    solution:   string,
    clues:      string[],
    difficulty: Difficulty
}

export interface IManuel1 extends ICreateGame {
    drawing: IDrawingCreator[]
}

export interface IDrawingCreator {
    color:  string,
    width:  number,
    points: IPoint[],
}

export interface IPoint {
    x:      number,
    y:      number,
    isEnd:  number,
}

export enum Difficulty {
    EASY    = "easy",
    MEDIUM  = "medium",
    HARD    = "hard",
}
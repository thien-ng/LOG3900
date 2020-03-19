export interface ISuggestion {
    drawing: string,
    object: string,
}

interface ICreateGame {
    solution:   string,
    clues:      string[],
    difficulty: Difficulty
}

export interface IManuel1 extends ICreateGame {
    drawing: IDrawing[]
}

export interface IDrawing {
    color:  string,
    width:  number,
    points: IPoints[],
}

export interface IPoints {
    x: number,
    y: number,
}

export enum Difficulty {
    EASY    = "easy",
    MEDIUM  = "medium",
    HARD    = "hard",
}
export interface ISuggestion {
    drawing: string,
    object: string,
}

export interface ICreateGame {
    gameName:   string,
    solution:   string,
    clues:      string[],
    // add other informations
}
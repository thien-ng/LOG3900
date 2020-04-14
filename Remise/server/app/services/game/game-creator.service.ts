import { injectable, inject } from "inversify";
import { ISuggestion, IManuel1, Difficulty, DisplayMode } from "../../interfaces/creator";
import { IGameRule } from "../../interfaces/rule";
import { RulesDbService } from "../../database/rules-db.service";
import { CreationAssist2 } from "./utils/creation-assists";

import Types from '../../types';

@injectable()
export class GameCreatorService {

    public constructor(@inject(Types.RulesDbService) private db: RulesDbService) {}

    public async getSuggestion(): Promise<ISuggestion> {
        return CreationAssist2.fetchSuggestion();
    }

    public async createGame(configs: IManuel1): Promise<void> {
        this.verifyConfigs(configs)
        this.transfromCluesAndSolutions(configs);

        const rule: IGameRule = {
            solution:       configs.solution,
            clues:          configs.clues,
            difficulty:     configs.difficulty,
            displayMode:    configs.displayMode,
            side:           configs.side,
            drawing:        configs.drawing,
        }
        await this.db.addRule(rule);
    }

    private verifyConfigs(configs: IManuel1): void {
        if (!configs.solution || /[^a-zA-Z ]/.test(configs.solution))
            throw new Error("Solution must contains letters only");
        if (!configs.clues ||configs.clues.length < 1)
            throw new Error("Some clues must be included in request");
        configs.clues.forEach(c => {
            if (/[^a-zA-Z ]/.test(c))
                throw new Error("Clues must contains letters only");
        });
        if (!configs.difficulty || !(configs.difficulty.toUpperCase() in Difficulty))
            throw new Error("Difficulty must be: easy, medium, or hard");
        if (!configs.displayMode || !(configs.displayMode.toUpperCase() in DisplayMode))
            throw new Error("Difficulty must be: classic, random, panoramic or centered");
        if (!configs.drawing || configs.drawing.length < 1)
            throw new Error("Drawings must be provided in the request");
    }

    private transfromCluesAndSolutions(configs: IManuel1): void {
        configs.solution = configs.solution.toLocaleLowerCase();
        const clues: string [] = [];
        configs.clues.forEach(c => {
            clues.push(c.toLocaleLowerCase().trim());
        })
        configs.clues = clues;
    }

}
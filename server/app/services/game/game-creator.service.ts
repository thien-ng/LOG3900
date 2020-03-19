import { injectable, inject } from "inversify";
import { ISuggestion, IManuel1, Difficulty } from "../../interfaces/creator";
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

        const rule: IGameRule = {
            solution:   configs.solution,
            clues:      configs.clues,
            difficulty: configs.difficulty,
            drawing:    configs.drawing
        }
        await this.db.addRule(rule);
    }

    private verifyConfigs(configs: IManuel1): void {
        if (!configs.solution || /[^a-zA-Z]/.test(configs.solution))
            throw new Error("Solution must contains letters only");
        if (!configs.clues ||configs.clues.length < 1)
            throw new Error("Some clues must be included in reuqest");            
        if (!configs.difficulty || !(configs.difficulty.toUpperCase() in Difficulty))
            throw new Error("Difficulty must be: easy, medium, or hard");
        if (!configs.drawing || configs.drawing.length < 1)
            throw new Error("Drawings must be provided in the request");
    }

}
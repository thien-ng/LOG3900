import { injectable, inject } from "inversify";
import { ISuggestion, ICreateGame } from "../../interfaces/creator";
import { IGameRule } from "../../interfaces/rule";
import { CardsDbService } from "../../database/cards-db.service";
import { CreationAssist2 } from "./utils/creation-assists";

import Types from '../../types';
import { uuid } from "uuidv4";

@injectable()
export class GameCreatorService {

    public constructor(@inject(Types.CardsDbService) private db: CardsDbService) {}

    public async getSuggestion(): Promise<ISuggestion> {
        return CreationAssist2.fetchSuggestion();
    }

    public createGame(configs: ICreateGame): void {
        const rule: IGameRule = {
            gameName: configs.gameName,
            gameID: uuid(),
            solution: configs.solution,
            clues: configs.clues,
        }
        this.db.addRule(rule);
    }

}
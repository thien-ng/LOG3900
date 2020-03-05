import { injectable, inject } from "inversify";
import { ISuggestion, ICreateGame } from "../../interfaces/creator";
import { IGameCard } from "../../interfaces/card";
import { uuid } from "uuidv4";
import { CardsDbService } from "../../database/cards-db.service";

import Types from '../../types';
import { CreationAssist2 } from "./utils/creation-assists";

@injectable()
export class GameCreatorService {

    public constructor(@inject(Types.CardsDbService) private db: CardsDbService) {}

    public async getSuggestion(): Promise<ISuggestion> {
        return CreationAssist2.fetchSuggestion();
    }

    public createGame(configs: ICreateGame): void {
        const card: IGameCard = {
            gameName: configs.gameName,
            gameID: uuid(),
            mode: configs.mode,
        }
        this.db.addCard(card);
    }

}
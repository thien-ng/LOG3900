import { injectable, inject } from "inversify";
import { ICreateGame, GameMode } from "../../interfaces/game";
import { IGameCard } from "../../interfaces/card";
import { uuid } from "uuidv4";
import { CardsDbService } from "../../database/cards-db.service";

import Types from '../../types';
import { CreationAssist2 } from "./utils/creation-assists";

@injectable()
export class GameCreatorService {

    public constructor(@inject(Types.CardsDbService) private db: CardsDbService) {}

    public async getSuggestion() {
        return CreationAssist2.fetchSuggestion();
    }

    public createGame(configs: ICreateGame): void {
        const card: IGameCard = {
            gameName: configs.gameName,
            gameID: uuid(),
            mode: GameMode.FFA,
        }
        this.db.addCard(card);
    }

}
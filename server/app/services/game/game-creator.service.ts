import { injectable, inject } from "inversify";
import { ICreateGame, IGameCard, GameMode } from "../../interfaces/game";
import { GameCardService } from "./game-card.service";
import { uuid } from "uuidv4";

import Types from '../../types';

@injectable()
export class GameCreatorService {

    public constructor(
        @inject(Types.GameCardService) private cardServ: GameCardService) {}

    public async getSuggestion() {
        // TODO: getSuggestion for assiste 2
    }

    public createGame(configs: ICreateGame): void {
        const card: IGameCard = {
            gameName: configs.gameName,
            uuid: uuid(),
            mode: GameMode.FFA,
        }
        this.cardServ.games.push(card);
    }

}
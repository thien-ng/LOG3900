import { injectable, inject } from "inversify";
import { ICreateGame } from "../../interfaces/game";
import { GameCardService } from "./game-card.service";

import Types from '../../types';

@injectable()
export class GameCreatorService {

    public constructor(
        @inject(Types.GameCardService) private cardServ: GameCardService) {}

    public async getSuggestion() {
        // TODO: getSuggestion for assiste 2
    }

    public createGame(configs: ICreateGame): void {
        this.cardServ.games.push(configs);
    }

}
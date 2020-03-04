import { injectable } from "inversify";
import { IGameCard } from "../../interfaces/game";

@injectable()
export class GameCardService {
    
    // TODO: this array should be in mongoDB
    public games: IGameCard[];

    public getGameCards(): IGameCard[] {
        return this.games;
    }

}
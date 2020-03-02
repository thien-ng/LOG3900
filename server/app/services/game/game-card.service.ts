import { injectable } from "inversify";
import { ICreateGame } from "../../interfaces/game";

@injectable()
export class GameCardService {
    
    // TODO: this array should be in mongoDB
    public games: ICreateGame[];

    public getGameCards(): ICreateGame[] {
        return this.games;
    }

}
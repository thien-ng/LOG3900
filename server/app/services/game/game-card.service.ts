import { injectable, inject } from "inversify";
import { IGameCard } from "../../interfaces/card";
import { CardsDbService } from "../../database/cards-db.service";

import Types from '../../types';
import { LobbyManagerService } from "./lobby-manager.service";

@injectable()
export class GameCardService {
    
    public constructor(
        @inject(Types.CardsDbService)       private db: CardsDbService,
        @inject(Types.LobbyManagerService)  private lobServ: LobbyManagerService) {
    }

    public async getGameCards(): Promise<IGameCard[]> {
        return this.db.getCards().then(cards => { return cards });
    }

    public async deleteCard(gameID: string): Promise<void> {
        const lobbies = this.lobServ.getActiveLobbies(gameID);
        console.log("lenght " + lobbies.length);
        
        if (lobbies && lobbies.length === 0) 
            this.db.deleteCard(gameID);
        else
            throw new Error("Some lobbies are still active with this game card");
    }

}
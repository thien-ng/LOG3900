import { injectable, inject } from "inversify";
import { IGameCard } from "../../interfaces/card";
import { CardsDbService } from "../../database/cards-db.service";

import Types from '../../types';

@injectable()
export class GameCardService {
    
    public constructor(@inject(Types.CardsDbService) private db: CardsDbService) {
        if(this.db){}
    }

    public async getGameCards(): Promise<IGameCard[]> {
        return await this.db.getCards();
    }

    public async deleteCard(gameID: string): Promise<void> {
        return await this.db.deleteCard(gameID);
    }

}
import { injectable } from "inversify";
import { DatabaseService } from "../database/database";
import { IGameInfo } from "../interfaces/game";

@injectable()
export class GameDbService extends DatabaseService {

    public async registerGame(info: IGameInfo): Promise<void> {
        // TODO register query
        this.pool.query(``);
    }

}
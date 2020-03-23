import { injectable } from "inversify";
import { DatabaseService } from "../database/database";
import { IGameInfo } from "../interfaces/game";

@injectable()
export class GameDbService extends DatabaseService {

    public async registerGame(info: IGameInfo): Promise<void> {
        this.pool.query(`SELECT LOG3900.registerGame(
                            CAST('${info.type}'     AS VARCHAR),
                            CAST('${info.date}'     AS VARCHAR),
                            CAST('${info.timer}'    AS INT),
                            CAST('${info.winner}'   AS VARCHAR),
                            CAST('${JSON.stringify(info.users)}'    AS JSON));`)
                            .then(() => {console.log("[DEBUG] added points to db successfully")})
                            .catch((e) => {console.log("[DEBUG] " + e.message)});
    }

}
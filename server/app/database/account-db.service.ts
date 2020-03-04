import { injectable } from "inversify";
import { DatabaseService } from "../database/database";
import { IRegistration, ILogin } from "../interfaces/communication";
import * as pg from "pg";
import { Time } from "../utils/date";

@injectable()
export class AccountDbService extends DatabaseService {

    public async registerAccount(registration: IRegistration): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.registerAccount(
                                    CAST('${registration.username}' AS VARCHAR),
                                    CAST('${registration.password}' AS VARCHAR),
                                    CAST('${registration.firstName}' AS VARCHAR),
                                    CAST('${registration.lastName}' AS VARCHAR));`);
    }

    public async loginAccount(login: ILogin): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.loginAccount(
                                    CAST('${login.username}' AS VARCHAR),
                                    CAST('${login.password}' AS VARCHAR));`);
    }

    public logConnection(username: string, is_login: boolean) {
        const time: string = Time.today();
        return this.pool.query(`SELECT LOG3900.logConnection(
                                    CAST('${username}' AS VARCHAR),
                                    CAST('${is_login}' AS BOOLEAN),
                                    CAST('${time}' AS VARCHAR));`);
    }

    public async getAccountInfoByName(username: string): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT (LOG3900.getAccountInfoByName(
            CAST('${username}' AS VARCHAR))).*;`);
    }
}

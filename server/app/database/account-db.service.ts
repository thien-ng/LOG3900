import { injectable } from "inversify";
import { DatabaseService } from "../database/database";
import { IRegistration, ILogin } from "../interfaces/communication";
import { Time } from "../utils/date";

import * as pg from "pg";

@injectable()
export class AccountDbService extends DatabaseService {

    public async registerAccount(registration: IRegistration): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.registerAccount(
                                CAST('${registration.username}' AS VARCHAR),
                                CAST('${registration.password}' AS VARCHAR),
                                CAST('${registration.firstName}' AS VARCHAR),
                                CAST('${registration.lastName}' AS VARCHAR));`).then((res) => {
                let avatar: Uint8Array;
                if (registration.avatar) {
                    avatar = registration.avatar;
                    return this.setAvatar(registration.username, avatar);
                }
                return res
            });
    }

    public async setAvatar(username: string, avatar: Uint8Array): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.setAvatar(
                                    CAST('${username}' AS VARCHAR),
                                    CAST('${avatar}' AS BYTEA));`);
    }

    public async loginAccount(login: ILogin): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.loginAccount(
                                    CAST('${login.username}' AS VARCHAR),
                                    CAST('${login.password}' AS VARCHAR));`);
    }

    public logConnection(username: string, isLogin: boolean): Promise<pg.QueryResult> {
        const time: string = Time.today();
        return this.pool.query(`SELECT LOG3900.logConnection(
                                    CAST('${username}' AS VARCHAR),
                                    CAST('${isLogin}' AS BOOLEAN),
                                    CAST('${time}' AS VARCHAR));`);
    }

    public async getAccountNamesByUsername(username: string): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT (LOG3900.getAccountNamesByUsername(
                                    CAST('${username}' AS VARCHAR))).*;`);
    }

    public async getAccountConnectionsByUsername(username: string): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT (LOG3900.getAccountConnectionsByUsername(
                                    CAST('${username}' AS VARCHAR))).*;`);
    }

    public async getProfileStats(username: string): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT (LOG3900.getProfileStats(
                                    CAST('${username}' AS VARCHAR))).*;`);
    }

    public async getGameIds(username: string): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT (LOG3900.getGameIds(
                                    CAST('${username}' AS VARCHAR))).*;`);
    }

    public async getGameInfo(gameId: number): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.getGameInfo(
                                    CAST('${gameId}' AS INT));`);
    }
}

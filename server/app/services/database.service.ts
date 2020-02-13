import { injectable } from "inversify";
import { IRegistration, ILogin } from "../interfaces/communication";
import { IChannelMessageDB } from '../interfaces/chat'; 
import * as pg from "pg";

@injectable()
export class DatabaseService {

    public connectionConfig: pg.ConnectionConfig = {
        user: "rfbpejtdulvzja",
        database: "dfcm6enu6paah0",
        password: "f0c366c68eec5e65232cad1820aceba731d8cc4d650b40e4d60e81a4bde2bc31",
        port: 5432,
        host: "ec2-174-129-253-86.compute-1.amazonaws.com",
        ssl: true,
        keepAlive : true
    };

    private pool: pg.Pool = new pg.Pool(this.connectionConfig);

    public constructor() {
        this.pool.connect().catch();
    }

    public async registerAccount(registration: IRegistration): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.registerAccount(
                                    CAST('${registration.username}' AS VARCHAR),
                                    CAST('${registration.password}' AS VARCHAR));`);
    }

    public async loginAccount(login: ILogin): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.loginAccount(
                                    CAST('${login.username}' AS VARCHAR),
                                    CAST('${login.password}' AS VARCHAR));`);
    }

    public async getAccountIdByUsername(username: string): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT a.id
                                FROM log3900.Account as a
                                WHERE  a.username = '${username}';`);
    }

    public async getMessagesWithChannelId(id: number): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT (LOG3900.getMessagesWithChannelId(
                                    CAST('${id}' AS INTEGER))).*;`);
    }

    public async createChannelWithAccountId(id: number): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.createChannelWithAccountId(
                                    CAST('${id}' AS INTEGER));`);
    }

    public async insertChannelMessage(mes: IChannelMessageDB): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT LOG3900.insertChannelMessage(
                                    CAST('${mes.channel_id}' AS INTEGER),
                                    CAST('${mes.account_id}' AS INTEGER),
                                    CAST('${mes.content}'    AS TEXT),
                                    CAST('${mes.time}'       AS VARCHAR));`);
    }

    public async getChannelsWithAccountName(username: string): Promise<pg.QueryResult> {
        return this.pool.query(`SELECT DISTINCT a.channel_id as out_id
                                FROM log3900.account as acc, log3900.accountchannel as a
                                WHERE acc.id = a.account_id
                                AND acc.username = '${username}';`);
    }

}
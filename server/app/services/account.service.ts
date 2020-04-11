import { injectable, inject } from "inversify";
import { IRegistration, IStatus, ILogin, IInfoUser, IConnection, IStats, IGame, IPlayer, IModeDate } from "../interfaces/communication";
import { AccountDbService } from "../database/account-db.service";
import * as pg from "pg";
import Types from '../types';

@injectable()
export class AccountService {

    public constructor(
        @inject(Types.AccountDbService) private database: AccountDbService) { }

    public async register(registration: IRegistration): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully registered account."
        };

        try {
            this.verifyRegistration(registration);
            await this.database.registerAccount(registration);
        } catch (e) {
            result.status = 400;
            result.message = e.message;
        }

        return result;
    }

    public async setAvatar(req: { username: string, avatar: string }): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully changed avatar"
        };

        try {
            this.verifyAvatar(req.avatar);
            await this.database.setAvatar(req.username, req.avatar);
        } catch (e) {
            result.status = 400;
            result.message = e.message;
        }

        return result;
    }

    public async getAvatar(username: string): Promise<string> {
        return this.database.getAvatar(username).then((res: pg.QueryResult) => {
            return res.rows[0];
        }).catch((e) => {
            return e;
        });
    }

    public async login(login: ILogin): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully logged in."
        };

        try {

            this.verifyLogin(login);
            await this.database.loginAccount(login);

        } catch (e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
    }

    private verifyRegistration(regis: IRegistration): void {
        if (regis.username.length < 1 || regis.username.length > 20) {
            throw new Error("username length should be between 1 and 20");
        }
        if (!/^[a-zA-Z0-9]+$/.test(regis.username)) {
            throw new Error("username must be alphanumeric");
        }
        if (regis.password.length < 1 || regis.password.length > 20) {
            throw new Error("password length should be between 1 and 20");
        }
        if (regis.firstName.length < 1 || regis.firstName.length > 100) {
            throw new Error("first name length should be between 1 and 100");
        }
        if (regis.lastName.length < 1 || regis.lastName.length > 100) {
            throw new Error("last name length should be between 1 and 100");
        }
        if (regis.avatar)
            this.verifyAvatar(regis.avatar);
    }

    private verifyAvatar(avatar: string): void {
        if (avatar.length < 1 || avatar.length > 400000) {
            throw new Error("avatar size should be under 300 KB in size; this file size is : " + (((avatar.length * 6) / 8) + 1) + "bytes");
        }
    }

    private verifyLogin(login: ILogin): void {
        if (login.username.length < 1 || login.username.length > 20) {
            throw new Error("username length should be between 1 and 20");
        }
        if (!/^[a-zA-Z0-9]+$/.test(login.username)) {
            throw new Error("username must be alphanumeric");
        }
        if (login.password.length < 1 || login.password.length > 20) {
            throw new Error("password length should be between 1 and 20");
        }
    }

    public async getUserInfo(username: string): Promise<IInfoUser> {
        const noms: Promise<any> = this.database.getAccountNamesByUsername(username).then((result: pg.QueryResult) => {
            return result.rows.map((row: any) => ({ username: username, lastName: row.out_lastname, firstName: row.out_firstname }))[0];
        }).catch((e) => {
            return e;
        });

        const connections: Promise<IConnection[]> = this.database.getAccountConnectionsByUsername(username).then((result: pg.QueryResult) => {
            return result.rows.map((row: any) => ({ username: username, isLogin: row.out_islogin, times: row.out_times })).reverse();
        }).catch((e) => {
            return e;
        });

        const profileStats: Promise<IStats> = this.database.getProfileStats(username).then((result: pg.QueryResult) => {
            return result.rows.map((row: any) => ({ totalGame: row.out_nbrgame, winRate: row.out_winrate, bestScore: row.out_best, totalPlayTime: row.out_elapsedtime, avgGameTime: row.out_timegame }))[0];
        }).catch((e) => {
            return e;
        });

        const games = this.getGameInfos(username).then((result: IGame[]) => {
            return result.reverse;
        }).catch((e) => {
            return e;
        });

        return Promise.all([noms, connections, profileStats, games]).then(async (res) => {

            return { username: username, firstName: res[0].firstName, lastName: res[0].lastName, connections: res[1], stats: res[2], games: res[3] };
        }).catch((e) => {
            return e;
        });
    }

    private async getGameInfos(username: string): Promise<IGame[]> {
        const mapDate = new Map<number, IModeDate>();
        const idList: number[] = [];
        const gameList: IGame[] = [];

        (await this.database.getGameIds(username)).rows.forEach(u => {
            mapDate.set(u.out_gamesid, { mode: u.out_mode, date: u.out_date });
            idList.push(u.out_gamesid);
        });

        for await (const id of idList) {
            const info = (await this.database.getGameInfo(id)).rows[0];

            const list: IPlayer[] = [];
            info.getgameinfo.forEach((p: IPlayer) => { list.push(p) });

            const obj = mapDate.get(id) as IModeDate;
            gameList.push({ mode: obj.mode, date: obj.date, players: list });
        }

        return gameList;
    }

}

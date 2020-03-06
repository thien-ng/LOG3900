import { injectable, inject } from "inversify";
import { IRegistration, IStatus, ILogin, IInfoUser, Iconnection } from "../interfaces/communication";
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
        if (regis.password.length < 1 || regis.password.length > 20) {
            throw new Error("password length should be between 1 and 20");
        }
        if (regis.firstName.length < 1 || regis.firstName.length > 100) {
            throw new Error("first name length should be between 1 and 100");
        }
        if (regis.lastName.length < 1 || regis.lastName.length > 100) {
            throw new Error("last name length should be between 1 and 100");
        }
    }

    private verifyLogin(login: ILogin): void {
        if (login.username.length < 1 || login.username.length > 20) {
            throw new Error("username length should be between 1 and 20");
        }
        if (login.password.length < 1 || login.password.length > 20) {
            throw new Error("password length should be between 1 and 20");
        }
    }

    public async getUserInfo(username: string): Promise<IInfoUser> {
        const noms: Promise<any> = this.database.getAccountNamesByUsername(username).then((result: pg.QueryResult) => {
            const res: any[] = result.rows.map((row: any) => ({ username: username, lastName: row.out_lastname, firstName: row.out_firstname }));
            return res[0];
        }).catch((e) => {
            return e;
        });
        const connections: Promise<Iconnection[]> = this.database.getAccountConnectionsByUsername(username).then((result: pg.QueryResult) => {
            console.log(result.rows);
            const res: Iconnection[] = result.rows.map((row: any) => ({ username: username, isLogin: row.out_islogin, times: row.out_times }));
            return res;
        }).catch((e) => {
            return e;
        });
        return Promise.all([noms, connections]).then((res) => {
            const out: IInfoUser = { username: username, firstName: res[0].firstName, lastName: res[0].lastName, connections: res[1] };
            return out;
        }).catch((e) => {
            return e;
        });
    }

}

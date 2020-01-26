import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import { IRegistration, IStatus, ILogin } from "../interfaces/communication";
import Types from '../types';

@injectable()
export class AccountService {

    public constructor(@inject(Types.DatabaseService) private database: DatabaseService) {}

    public async register(registration: IRegistration): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully registered account."
        };

        try {
            await this.database.registerAccount(registration);
        } catch(e) {
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
            await this.database.loginAccount(login);
        } catch(e) {
            result.status = 400
            result.message = e.message;
        }

        return result;
    }

}
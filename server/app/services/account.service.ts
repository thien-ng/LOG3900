import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import { IRegistration, IStatus } from "../interfaces/communication";
import Types from '../types';

@injectable()
export class AccountService {

    public constructor(@inject(Types.DatabaseService) private databaseService: DatabaseService) {
        
    }

    public async register(registration: IRegistration): Promise<IStatus> {
        let result: IStatus = {
            status: 200,
            message: "Succesfully registered account"
        };

        try {
            await this.databaseService.registerAccount(registration);
        } catch(e) {
            result.status = 400;
            result.message = e.message;
        }

        return result;
    }

}
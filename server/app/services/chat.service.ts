import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import Types from '../types';
import { IUser } from "../interfaces/user-manager";

@injectable()
export class ChatService {

    private channelMapUsersList: Map<number, IUser[]>;

    public constructor(@inject(Types.ChatService) db: DatabaseService) {
        this.channelMapUsersList = new Map<number, IUser[]>();
    }

    public addUserToChannelMap(user: IUser): void {
        console.log(this.channelMapUsersList);
        
    }


}
import { injectable, inject } from "inversify";
import { DatabaseService } from "./database.service";
import Types from '../types';

@injectable()
export class ChatService {

    public constructor(@inject(Types.ChatService) db: DatabaseService) {}

    // TODO: add chat services body

}
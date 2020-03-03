import { IUser } from "../../interfaces/user-manager";

export class Arena {

    private users: IUser[];
    private room: string;
    private size: number;
    // TODO add attribute game rule search by the uuid

    public constructor(users: IUser[], size: number, room: string) {
        this.users = users;
        this.room = room;
        this.size = size;
        
        if (this.users || this.size || this.room) {}
    }
    
}
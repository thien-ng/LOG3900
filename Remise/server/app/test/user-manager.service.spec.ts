import * as chai from "chai";
import * as spies from "chai-spies";
import { UserManagerService } from "../services/user-manager.service";
import Types from '../types';
import { container } from "../inversify.config";

chai.use(spies);

describe("UserManagementService", () => {

    let service: UserManagerService;

    beforeEach(() => {
        container.snapshot();
        service = container.get<UserManagerService>(Types.UserManagerService);
    });

    afterEach(() => {
        container.restore();
    });

    it("Should add and delete user succesfully", () => {

        service.addUser({username:"username", socketId: "socket"});

        chai.expect(service["users"].length).to.be.equal(1);

        service.deleteUser("username");

        chai.expect(service["users"].length).to.be.equal(0);
    });

    it("Should find user correctly in lists of users", () => {
        //Given
        service.addUser({username:"username1", socketId: "socket1"});
        service.addUser({username:"username2", socketId: "socket2"});

        //When
        const result = service.checkIfUserIsOnline("username2");
        
        //Then
        chai.expect(result).to.be.true;
    });

    it("Should not find user in lists of users", () => {
        //Given
        service.addUser({username:"username1", socketId: "socket1"});

        //When
        const result = service.checkIfUserIsOnline("username2");
        
        //Then
        chai.expect(result).to.be.false;
    });

    it("Should return all users in list", () => {
        //Given
        service.addUser({username:"user1", socketId: "socket1"});
        service.addUser({username:"user2", socketId: "socket2"});
        service.addUser({username:"user3", socketId: "socket3"});

        //When
        const result = service.getOnlineUsers();
        
        //Then
        chai.expect(result).to.deep.equal(["user1", "user2", "user3"]);
    });

});
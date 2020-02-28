import * as chai from "chai";
import * as spies from "chai-spies";

import Types from '../../types';
import { container } from "../../inversify.config";
import { LobbyManagerService } from "../../services/game/lobby-manager.service";
import { IJoinLobby, IActiveLobby, ILeaveLobby } from "../../interfaces/game";

chai.use(spies);

describe("LobbyManagerService", () => {

    let service: LobbyManagerService;

    beforeEach(() => {
        container.snapshot();
        service = container.get<LobbyManagerService>(Types.LobbyManagerService);
    });

    afterEach(() => {
        container.restore();
    });

    it("Should fail when joining lobby but socket is not connected", async () => {
        //given
        const req: IJoinLobby = {username:"", private: true, lobbyName: "name", password: "password", size: 2};

        //when
        //then
        try {service.join(req)} 
        catch(e) {chai.expect(e.message).to.equal("Socket is not connected")};
    });

    it("Should fail when joining lobby with incorrect username", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        const req1: IJoinLobby = {username:"", private: true, lobbyName: "name", password: "password", size: 2};
        const req2: IJoinLobby = {username:"LongerThan20Character", private: true, lobbyName: "name", password: "password", size: 2};

        //when
        //then
        try {service.join(req1)} catch(e) {chai.expect(e.message).to.equal("Username lenght must be between 1 and 20")};
        try {service.join(req2)} catch(e) {chai.expect(e.message).to.equal("Username lenght must be between 1 and 20")};
    });

    it("Should fail when joining private lobby without password", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        const req: IJoinLobby = {username:"aaa", private: true, lobbyName: "name", password: undefined, size: 2};

        //when
        //then
        try {service.join(req)} 
        catch(e) {chai.expect(e.message).to.equal("Private lobby must have password")};
    });

    it("Should fail when joining private lobby with incorrect password length", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        const req: IJoinLobby = {username:"aaa", private: true, lobbyName: "name", password: "LongerThan20Character", size: 2};

        //when
        //then
        try {service.join(req)} catch(e) {chai.expect(e.message).to.equal("Password lenght must be between 1 and 20")};
    });

    it("Should fail when lobby size is not in correct range", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        const req1: IJoinLobby = {username:"aaa", private: true, lobbyName: "name", password: "LongerThan20Character", size: 0};
        const req2: IJoinLobby = {username:"aaa", private: true, lobbyName: "name", password: "LongerThan20Character", size: 11};
        
        //when
        //then
        try {service.join(req1)} catch(e) {chai.expect(e.message).to.equal("Lobby size should be between 1 and 10")};
        try {service.join(req2)} catch(e) {chai.expect(e.message).to.equal("Lobby size should be between 1 and 10")};
    });

    it("Should fail when joining when user is not found in online users", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        const req: IJoinLobby = {username:"username", private: true, lobbyName: "name", password: "password", size: 2};

        //when
        //then
        try {service.join(req)} catch(e) {chai.expect(e.message).to.equal("username is not found in logged users")};
    });

    it("Should fail when joining when user is already in lobby", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service, "sendMessages", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})
        const req: IJoinLobby = {username:"username", private: true, lobbyName: "name", password: "password", size: 2};
        service.join(req)

        //when
        //then
        try {service.join(req)} catch(e) {chai.expect(e.message).to.equal("username is already in lobby name")};
    });

    it("Should fail when max users in lobby is reached", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})

        const user1 = {username:"username1", socketId: "testId"};
        const user2 = {username:"username2", socketId: "testId"};
        service["lobbies"].set("name", {users: [user1, user2], private: true, lobbyName:"name", password: "password", size: 2})

        const req: IJoinLobby = {username:"username", private: true, lobbyName: "name", password: "password"};

        //when
        //then
        try {service.join(req)} catch(e) {chai.expect(e.message).to.equal("Max number of users in lobby name reached")};
    });

    it("Should create a new loby when joining empty lobby", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service, "sendMessages", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})

        const req: IJoinLobby = {username:"username", private: true, lobbyName: "name", password: "password", size: 2};

        //when
        const result = service.join(req);

        //then
        const lobby = service["lobbies"].get("name") as IActiveLobby;
        chai.expect(lobby.users.length).to.equal(1);
        chai.expect(result).to.equal("Successfully joined lobby name");
    });

    it("Should join active public lobby successfully", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service, "sendMessages", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})
        const spy = chai.spy.on(service, "isPwdMatching");
        
        const user = {username:"test", socketId: "testId"};
        service["lobbies"].set("name", {users: [user], private: false, lobbyName:"name", size: 2})

        const req: IJoinLobby = {username:"username", private: false, lobbyName: "name", size: 2};

        //when
        const result = service.join(req);

        //then
        const lobby = service["lobbies"].get("name") as IActiveLobby;
        chai.expect(lobby.users.length).to.equal(2);
        chai.expect(result).to.equal("Successfully joined lobby name");
        chai.expect(spy).to.not.have.been.called();
    });

    it("Should join active private lobby successfully", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service, "sendMessages", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})
        const spy = chai.spy.on(service, "isPwdMatching");
        
        const user = {username:"test", socketId: "testId"};
        service["lobbies"].set("name", {users: [user], private: true, lobbyName:"name", password: "password", size: 2})

        const req: IJoinLobby = {username:"username", private: true, lobbyName: "name", password: "password", size: 2};

        //when
        const result = service.join(req);

        //then
        const lobby = service["lobbies"].get("name") as IActiveLobby;
        chai.expect(lobby.users.length).to.equal(2);
        chai.expect(result).to.equal("Successfully joined lobby name");
        chai.expect(spy).have.been.called();
    });

    it("Should fail joining active private lobby when password is incorrect", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})
        const spy = chai.spy.on(service, "isPwdMatching");
        
        const user = {username:"test", socketId: "testId"};
        service["lobbies"].set("name", {users: [user], private: true, lobbyName:"name", password: "password", size: 2})

        const req: IJoinLobby = {username:"username", private: true, lobbyName: "name", password: "incorrectPW", size: 2};

        //when
        
        //then
        try {service.join(req)}
        catch(e) {chai.expect(e.message).to.equal("Wrong password for lobby name")};
        const lobby = service["lobbies"].get("name") as IActiveLobby;
        chai.expect(lobby.users.length).to.equal(1);
        chai.expect(spy).have.been.called();
    });

    it("Should leave lobby successfully when lobby has one user", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service, "sendMessages", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})
        
        const user = {username:"username", socketId: "testId"};
        service["lobbies"].set("name", {users: [user], private: true, lobbyName:"name", password: "password", size: 2})

        const req: ILeaveLobby = {username:"username", lobbyName: "name"};

        //when
        const result = service.leave(req);

        //then
        const lobby = service["lobbies"].get("name");
        chai.expect(lobby).to.equal(undefined);
        chai.expect(result).to.equal("Left name successfully");
    });

    it("Should leave lobby successfully when lobby has two user", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service, "sendMessages", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})
        
        const user1 = {username:"username", socketId: "testId"};
        const user2 = {username:"username2", socketId: "testId"};
        service["lobbies"].set("name", {users: [user1, user2], private: true, lobbyName:"name", password: "password", size: 2})

        const req: ILeaveLobby = {username:"username", lobbyName: "name"};

        //when
        const result = service.leave(req);

        //then
        const lobby = service["lobbies"].get("name") as IActiveLobby;
        chai.expect(lobby.users.length).to.equal(1);
        chai.expect(result).to.equal("Left name successfully");
    });

    it("Should leave lobby successfully when lobby has two user", async () => {
        //given
        chai.spy.on(service, "verifySocketConnection", () => {});
        chai.spy.on(service["userServ"], "getUsersByName", () => {return {username:"username", socketId: "id"}})
        
        const req: ILeaveLobby = {username:"username", lobbyName: "name"};

        //when
        //then
        try {service.leave(req)}
        catch(e) {chai.expect(e.message).to.equal("name not found")};
    });

});
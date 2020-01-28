import * as chai from "chai";
import * as spies from "chai-spies";
import { UserManagerService } from "../services/user-manager.service";
import Types from '../types';
import { container } from "../inversify.config";
import * as ws from 'ws';
import { mock } from "ts-mockito";

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
        const setSpy = chai.spy.on(service["usersMap"], "set");
        const deleteSpy = chai.spy.on(service["usersMap"], "delete");

        const socket: ws = mock(ws);
        service.addUser("user", socket);
        service.deleteUser("user");

        chai.expect(setSpy).to.have.been.called;
        chai.expect(deleteSpy).to.have.been.called;
    });

});
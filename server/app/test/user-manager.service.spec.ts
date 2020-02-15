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

        service.addUser("username");

        chai.expect(service["users"].length).to.be.equal(1);

        service.deleteUser("username");

        chai.expect(service["users"].length).to.be.equal(0);
    });

});
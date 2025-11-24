package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should send email notification")
    request {
        method POST()
        url("/email") {}
        headers {
            contentType(applicationJson())
        }
        body(
                to: "test@test.ru",
                subject: "Test",
                text: "text"
        )
    }
    response {
        status OK()
    }
}

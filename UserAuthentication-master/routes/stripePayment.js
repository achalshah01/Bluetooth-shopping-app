const express = require("express");
const router = express.Router();
const app = express();
const stripe = require("stripe")("sk_test_9fqGx6fFuZeNeU2OUyZBhWR900Wr4RtnlH");
const auth = require("../middleware/auth");

router.post("/customer", auth, (req, res) => {
	if (res) {
		const { email, stripeToken } = req.body;
		stripe.customers.create(
			{
				source: stripeToken,
				email: email
			},
			(err, id) => {
				if (id) {
					res.json({ id });
				} else {
					res.json({ err });
				}
			}
		);
	}
});

router.post("/saveCard", auth, (req, res) => {
	if (res) {
		const { id, stripeToken } = req.body;
		stripe.customers.createSource(
			id,
			{
				source: stripeToken
			},
			(err, card) => {
				if (card) {
					res.json({ card });
				} else {
					res.json({ err });
				}
			}
		);
	}
});

router.post("/listCard", auth, (req, res) => {
	if (res) {
		const { id } = req.body;
		stripe.customers.listSources(
			id,
			{
				limit: 3,
				object: "card"
			},
			(err, cards) => {
				if (cards) {
					res.json({ cards });
				} else {
					res.json({ err });
				}
			}
		);
	}
});

router.post("/retriveCard", auth, (req, res) => {
	if (res) {
		const { id, stripeToken } = req.body;
		stripe.customers.retrieveSource(id, stripeToken, (err, id) => {
			if (id) {
				res.json({ id });
			} else {
				res.json({ err });
			}
		});
	}
});

router.post("/updateDefault", auth, (req, res) => {
	if (res) {
		const { id, stripeToken } = req.body;
		stripe.customers.update(
			id,
			{
				source: stripeToken
			},
			(err, id) => {
				if (id) {
					res.json({ id });
				} else {
					res.json({ err });
				}
			}
		);
	}
});

router.post("/", auth, (req, res) => {
	if (res) {
		const { amount, stripeToken } = req.body;

		stripe.charges.create(
			{
				amount,
				currency: "usd",
				//description: "Example charge",
				//customer: id
				source: stripeToken
				//metadata: { order_id: req.user.id }
			},
			(err, key) => {
				if (key) {
					res.json({ key });
				} else {
					res.json({ err });
				}
			}
		);
	}
});

router.post("/paymentAuth", auth, (req, res) => {
	if (res) {
		const { amount, stripeToken, id } = req.body;

		stripe.paymentIntents.create(
			{
				amount,
				currency: "usd",
				payment_method_types: ["card"],
				//description: "Example charge",
				customer: id,
				off_session: true,
				confirm: true,
				payment_method: stripeToken

				//source: stripeToken,
				//metadata: { order_id: req.user.id }
			},
			(err, key) => {
				if (key) {
					res.json({ key });
				} else {
					res.json({ err });
				}
			}
		);
	}
});

router.post("/oneTimePayment", auth, (req, res) => {
	if (res) {
		const { amount, stripeToken, id } = req.body;

		stripe.charges.create(
			{
				amount,
				currency: "usd",
				description: "Example charge",
				//customer: id,
				source: stripeToken,
				metadata: { order_id: req.user.id }
			},
			(err, key) => {
				if (key) {
					res.json({ key });
				} else {
					res.json({ err });
				}
			}
		);
	}
});

router.delete("/", auth, (req, res) => {
	if (res) {
		const { id, stripeToken } = req.body;
		stripe.customers.deleteSource(id, stripeToken, (err, id) => {
			if (id) {
				res.json({ id });
			} else {
				res.json({ err });
			}
		});
	}
});

module.exports = router;

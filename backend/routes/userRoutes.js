import express from "express"
import { getUsers, authLogin } from '../controller/userController.js'

const router = express.Router()

router.route('/').get(getUsers)
router.route('/').post(authLogin)

export default router
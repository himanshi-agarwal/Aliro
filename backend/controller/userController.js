import mongoose from "mongoose"
import User from '../models/userModel.js'

const getUsers = (async(req, res) => {
    res.json([
        { "userName": "John", "password": "pass123", "email": "john@example.com", "contactNo": 1234567890, "userType": "Visitor" },
        { "userName": "Jane", "password": "pass456", "email": "jane@example.com", "contactNo": 9876543210, "userType": "Admin" }
    ])
})

const authLogin = (async(req, res) => {
    const name = req.body.name
    const email = req.body.email
    const password = req.body.password
    const contactNo = req.body.contactNo
    const userType = req.body.userType
})

export { getUsers, authLogin}
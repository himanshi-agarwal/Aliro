import express from "express";
import dotenv from "dotenv";
import userRoutes from './routes/userRoutes.js'
import connectDB from "./config/db.js";

dotenv.config()
connectDB()

const port = process.env.PORT || 5000
const app = express()

app.use(express.json())
app.use(express.urlencoded({ extended: true}))

app.get('/', (req, res) => {
    res.send("Server Running")
})

app.use('/user', userRoutes)

app.listen(port, () => {
    console.log(`Server running on port ${port}`)
})
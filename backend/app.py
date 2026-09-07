from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import (
    JWTManager, create_access_token, jwt_required, get_jwt_identity
)
import os

app = Flask(__name__)

# Configuración de la Base de Datos
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///site.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Configuración de JWT
app.config['JWT_SECRET_KEY'] = os.environ.get('JWT_SECRET_KEY', 'clave-temporal-dev')

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
jwt = JWTManager(app)

# ---------- MODELOS ----------

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(20), unique=True, nullable=False)
    password = db.Column(db.String(60), nullable=False)

    def __repr__(self):
        return f"User('{self.username}')"

class Task(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(120), nullable=False)
    done = db.Column(db.Boolean, default=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    def to_dict(self):
        return {"id": self.id, "title": self.title, "done": self.done}

# ---------- VERIFICACIÓN Y AUTENTICACIÓN ----------

@app.route('/')
def hello():
    return jsonify({"message": "API Funcionando"})

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"message": "Usuario y contraseña son obligatorios"}), 400

    if User.query.filter_by(username=username).first():
        return jsonify({"message": "El usuario ya existe"}), 400

    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')
    new_user = User(username=username, password=hashed_password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": "Usuario creado exitosamente"}), 201

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    user = User.query.filter_by(username=username).first()

    if user and bcrypt.check_password_hash(user.password, password):
        token = create_access_token(identity=str(user.id))
        return jsonify({
            "status": "success",
            "message": "Login exitoso",
            "access_token": token,
            "user_id": user.id,
            "username": user.username
        }), 200
    else:
        return jsonify({"status": "error", "message": "Credenciales inválidas"}), 401

# ---------- CRUD (protegido con JWT) ----------

@app.route('/tasks', methods=['POST'])
@jwt_required()
def create_task():
    data = request.get_json()
    title = data.get('title')

    if not title:
        return jsonify({"message": "El campo 'title' es obligatorio"}), 400

    user_id = get_jwt_identity()
    task = Task(title=title, user_id=user_id)
    db.session.add(task)
    db.session.commit()

    return jsonify(task.to_dict()), 201

@app.route('/tasks', methods=['GET'])
@jwt_required()
def get_tasks():
    user_id = get_jwt_identity()
    tasks = Task.query.filter_by(user_id=user_id).all()
    return jsonify([t.to_dict() for t in tasks]), 200

@app.route('/tasks/<int:task_id>', methods=['PUT'])
@jwt_required()
def update_task(task_id):
    user_id = get_jwt_identity()
    task = Task.query.filter_by(id=task_id, user_id=user_id).first()

    if not task:
        return jsonify({"message": "Tarea no encontrada"}), 404

    data = request.get_json()
    task.title = data.get('title', task.title)
    task.done = data.get('done', task.done)
    db.session.commit()

    return jsonify(task.to_dict()), 200

@app.route('/tasks/<int:task_id>', methods=['DELETE'])
@jwt_required()
def delete_task(task_id):
    user_id = get_jwt_identity()
    task = Task.query.filter_by(id=task_id, user_id=user_id).first()

    if not task:
        return jsonify({"message": "Tarea no encontrada"}), 404

    db.session.delete(task)
    db.session.commit()

    return '', 204

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(host='0.0.0.0', port=5000, debug=True)
import os
import datetime
from flask import Flask, render_template, request, redirect, url_for, flash
from flask_sqlalchemy import SQLAlchemy

# --- KONFIGURASI APLIKASI ---
app = Flask(__name__)
app.config['SECRET_KEY'] = 'kunci-rahasia-yang-sangat-aman'
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@localhost/db_rekomendasi_software'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)

@app.context_processor
def inject_current_year():
    return {'current_year': datetime.date.today().year}

# --- MODEL DATABASE (SQLAlchemy) ---
class Alternatif(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    nama = db.Column(db.String(100), nullable=False)
    deskripsi = db.Column(db.String(200))
    nilai = db.relationship('NilaiMatriks', backref='alternatif', lazy=True, cascade="all, delete-orphan")

class Kriteria(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    nama = db.Column(db.String(100), nullable=False)
    bobot = db.Column(db.Float, nullable=False)
    jenis = db.Column(db.String(10), nullable=False)
    nilai = db.relationship('NilaiMatriks', backref='kriteria', lazy=True, cascade="all, delete-orphan")

class NilaiMatriks(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    alternatif_id = db.Column(db.Integer, db.ForeignKey('alternatif.id'), nullable=False)
    kriteria_id = db.Column(db.Integer, db.ForeignKey('kriteria.id'), nullable=False)
    nilai = db.Column(db.Float, nullable=False)

# --- ROUTE & LOGIKA APLIKASI ---
@app.route('/')
def index():
    alternatifs = Alternatif.query.order_by(Alternatif.id).all()
    kriterias = Kriteria.query.order_by(Kriteria.id).all()
    nilai_matriks = NilaiMatriks.query.all()
    nilai_dict = {(n.alternatif_id, n.kriteria_id): n.nilai for n in nilai_matriks}
    return render_template('index.html', alternatifs=alternatifs, kriterias=kriterias, nilai_dict=nilai_dict)

@app.route('/hasil')
def hasil():
    alternatifs = Alternatif.query.all()
    kriterias = Kriteria.query.all()
    
    if not alternatifs or not kriterias:
        flash('Data alternatif atau kriteria masih kosong. Silakan isi terlebih dahulu.', 'warning')
        return redirect(url_for('index'))

    nilai_matriks = NilaiMatriks.query.all()
    
    matriks_r = {}
    for alt in alternatifs:
        matriks_r[alt.id] = {}
        for krt in kriterias:
            nilai_entry = next((n for n in nilai_matriks if n.alternatif_id == alt.id and n.kriteria_id == krt.id), None)
            matriks_r[alt.id][krt.id] = nilai_entry.nilai if nilai_entry else 0.0

    matriks_n = {}
    for krt in kriterias:
        kolom_nilai = [matriks_r[alt.id][krt.id] for alt in alternatifs]
        min_val = min(kolom_nilai) if kolom_nilai else 0
        max_val = max(kolom_nilai) if kolom_nilai else 0
        
        for alt in alternatifs:
            if alt.id not in matriks_n: matriks_n[alt.id] = {}
            nilai_r = matriks_r[alt.id][krt.id]
            if krt.jenis.lower() == 'benefit':
                matriks_n[alt.id][krt.id] = nilai_r / max_val if max_val != 0 else 0
            elif krt.jenis.lower() == 'cost':
                matriks_n[alt.id][krt.id] = min_val / nilai_r if nilai_r != 0 else 0
    
    hasil_akhir = []
    for alt in alternatifs:
        total_nilai = sum(krt.bobot * matriks_n[alt.id][krt.id] for krt in kriterias)
        hasil_akhir.append({'alternatif': alt, 'nilai': total_nilai})
        
    hasil_akhir.sort(key=lambda x: x['nilai'], reverse=True)
    return render_template('hasil.html', hasil=hasil_akhir)

@app.route('/tambah_alternatif', methods=['POST'])
def tambah_alternatif():
    nama = request.form.get('nama')
    deskripsi = request.form.get('deskripsi')
    if nama:
        db.session.add(Alternatif(nama=nama, deskripsi=deskripsi))
        db.session.commit()
    return redirect(url_for('index'))

@app.route('/hapus_alternatif/<int:id>')
def hapus_alternatif(id):
    alt = db.get_or_404(Alternatif, id)
    db.session.delete(alt)
    db.session.commit()
    return redirect(url_for('index'))

@app.route('/tambah_kriteria', methods=['POST'])
def tambah_kriteria():
    nama = request.form.get('nama')
    bobot = request.form.get('bobot')
    jenis = request.form.get('jenis')
    if nama and bobot and jenis:
        db.session.add(Kriteria(nama=nama, bobot=float(bobot.replace(',', '.')), jenis=jenis))
        db.session.commit()
    return redirect(url_for('index'))

@app.route('/hapus_kriteria/<int:id>')
def hapus_kriteria(id):
    krt = db.get_or_404(Kriteria, id)
    db.session.delete(krt)
    db.session.commit()
    return redirect(url_for('index'))

@app.route('/simpan_nilai', methods=['POST'])
def simpan_nilai():
    for key, value in request.form.items():
        if key.startswith('nilai-'):
            _, alt_id, krt_id = key.split('-')
            alt_id, krt_id = int(alt_id), int(krt_id)
            nilai_obj = NilaiMatriks.query.filter_by(alternatif_id=alt_id, kriteria_id=krt_id).first()
            nilai_float = float(value.replace(',', '.')) if value else 0.0
            if nilai_obj:
                nilai_obj.nilai = nilai_float
            else:
                db.session.add(NilaiMatriks(alternatif_id=alt_id, kriteria_id=krt_id, nilai=nilai_float))
    db.session.commit()
    flash('Nilai berhasil disimpan!', 'success')
    return redirect(url_for('index'))

@app.cli.command("init-db")
def init_db_command():
    with app.app_context():
        db.drop_all()
        db.create_all()
        db.session.add_all([
            Alternatif(nama='Adobe Premiere Pro', deskripsi='Standar industri, fitur lengkap.'),
            Alternatif(nama='DaVinci Resolve', deskripsi='Gratis, color grading terbaik.'),
            Alternatif(nama='Final Cut Pro', deskripsi='Eksklusif Mac, performa cepat.'),
            Alternatif(nama='Filmora', deskripsi='Mudah digunakan untuk pemula.')
        ])
        db.session.add_all([
            Kriteria(nama='Kemudahan Penggunaan', bobot=0.30, jenis='benefit'),
            Kriteria(nama='Fitur Editing', bobot=0.25, jenis='benefit'),
            Kriteria(nama='Kompatibilitas OS', bobot=0.20, jenis='benefit'),
            Kriteria(nama='Biaya Lisensi', bobot=0.15, jenis='cost'),
            Kriteria(nama='Kecepatan Rendering', bobot=0.10, jenis='benefit')
        ])
        db.session.commit()
        print("Database telah diinisialisasi dengan data baru.")

if __name__ == '__main__':
    app.run(debug=True)